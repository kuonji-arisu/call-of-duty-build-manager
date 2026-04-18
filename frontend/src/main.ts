import { createApp } from "vue";

import App from "./App.vue";
import { naive } from "./naive";
import { pinia } from "./pinia";
import router from "./router";
import "./styles.css";

const app = createApp(App);

app.use(naive);
app.use(pinia);
app.use(router);
app.mount("#app");
