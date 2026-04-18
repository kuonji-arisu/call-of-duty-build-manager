package io.github.kuonjiarisu.backend.auth.service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import io.github.kuonjiarisu.backend.auth.config.AuthProperties;
import io.github.kuonjiarisu.backend.auth.model.CaptchaResponse;
import io.github.kuonjiarisu.backend.support.DomainSupport;

@Service
public class CaptchaService {

    private static final String CAPTCHA_KEY_PREFIX = "auth:captcha:";
    private static final char[] CAPTCHA_CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray();

    private final StringRedisTemplate stringRedisTemplate;
    private final AuthProperties authProperties;

    public CaptchaService(
        StringRedisTemplate stringRedisTemplate,
        AuthProperties authProperties
    ) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.authProperties = authProperties;
    }

    public CaptchaResponse createCaptcha() {
        var captchaId = UUID.randomUUID().toString();
        var captchaCode = randomCaptchaCode();
        var ttl = Duration.ofMinutes(authProperties.captchaTtlMinutes());

        stringRedisTemplate.opsForValue().set(redisKey(captchaId), captchaCode.toLowerCase(), ttl);

        return new CaptchaResponse(
            captchaId,
            toSvgDataUrl(captchaCode),
            ttl.toSeconds()
        );
    }

    public void validateAndConsume(String captchaId, String captchaCode) {
        var normalizedCaptchaId = DomainSupport.requireText(captchaId, "验证码 ID");
        var normalizedCode = DomainSupport.requireText(captchaCode, "验证码").toLowerCase();
        var key = redisKey(normalizedCaptchaId);
        var expected = stringRedisTemplate.opsForValue().get(key);

        if (expected == null) {
            throw new IllegalArgumentException("验证码已过期，请重新获取");
        }

        stringRedisTemplate.delete(key);

        if (!expected.equals(normalizedCode)) {
            throw new IllegalArgumentException("验证码错误");
        }
    }

    private String randomCaptchaCode() {
        var builder = new StringBuilder(4);
        for (int index = 0; index < 4; index += 1) {
            builder.append(CAPTCHA_CHARS[ThreadLocalRandom.current().nextInt(CAPTCHA_CHARS.length)]);
        }
        return builder.toString();
    }

    private String toSvgDataUrl(String captchaCode) {
        var svg = """
            <svg xmlns="http://www.w3.org/2000/svg" width="132" height="44" viewBox="0 0 132 44">
              <rect width="132" height="44" rx="10" fill="#111827"/>
              <path d="M8 34 C30 8, 55 8, 74 30 S106 38, 124 10" stroke="#2dd4bf" stroke-width="2" fill="none" opacity="0.35"/>
              <text x="66" y="29" text-anchor="middle" font-family="Verdana,Segoe UI,sans-serif" font-size="24" letter-spacing="6" fill="#f9fafb">%s</text>
            </svg>
            """.formatted(captchaCode);

        return "data:image/svg+xml;base64," + Base64.getEncoder().encodeToString(svg.getBytes(StandardCharsets.UTF_8));
    }

    private String redisKey(String captchaId) {
        return CAPTCHA_KEY_PREFIX + captchaId;
    }
}
