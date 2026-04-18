import { request } from "../client";
import type { InitialData } from "../../shared/types";

export const loadInitialData = () => request<InitialData>("/public/bootstrap");
