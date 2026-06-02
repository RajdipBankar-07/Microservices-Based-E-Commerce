const BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

interface RequestOptions extends RequestInit {
  bodyData?: any;
}

export interface ApiError {
  message: string;
  status?: number;
}

async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const url = `${BASE_URL}${path}`;

  // Build headers
  const headers = new Headers(options.headers || {});
  
  // Set JSON content type by default unless body is FormData
  if (!(options.body instanceof FormData)) {
    headers.set("Content-Type", "application/json");
  }

  // Get token from localStorage
  if (typeof window !== "undefined") {
    const token = localStorage.getItem("token");
    if (token) {
      // Clean token in case it has Bearer prefix or double quotes
      const cleanToken = token.replace(/^Bearer\s+/, "").replace(/^"(.*)"$/, "$1");
      headers.set("Authorization", `Bearer ${cleanToken}`);
    }
  }

  const config: RequestInit = {
    ...options,
    headers,
  };

  if (options.bodyData) {
    config.body = JSON.stringify(options.bodyData);
  }

  try {
    const response = await fetch(url, config);

    // Handles text responses (e.g. for String responses or empty bodies)
    const contentType = response.headers.get("content-type");
    let data: any;
    
    if (contentType && contentType.includes("application/json")) {
      data = await response.json();
    } else {
      data = await response.text();
    }

    if (!response.ok) {
      // If error message is wrapped in an ApiResponse DTO
      const errorMessage = data?.message || data || `Request failed with status ${response.status}`;
      const error: ApiError = {
        message: errorMessage,
        status: response.status,
      };
      throw error;
    }

    return data as T;
  } catch (error: any) {
    if (error.message) {
      throw error as ApiError;
    }
    throw {
      message: error.message || "Something went wrong. Please check your connection.",
    } as ApiError;
  }
}

export const api = {
  get: <T>(path: string, options?: RequestOptions) => 
    request<T>(path, { ...options, method: "GET" }),
    
  post: <T>(path: string, body?: any, options?: RequestOptions) => 
    request<T>(path, { ...options, method: "POST", bodyData: body }),
    
  put: <T>(path: string, body?: any, options?: RequestOptions) => 
    request<T>(path, { ...options, method: "PUT", bodyData: body }),
    
  delete: <T>(path: string, options?: RequestOptions) => 
    request<T>(path, { ...options, method: "DELETE" }),
};
