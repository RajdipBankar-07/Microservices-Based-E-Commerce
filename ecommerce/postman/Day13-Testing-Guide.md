# Day 13 Testing Guide (Postman)

Goal:
- Login returns JWT token
- Protected APIs (`/products/**`, `/orders/**`) require `Authorization: Bearer <token>`

## Import Collection
- Open Postman
- Click Import
- Select `postman/Day13-Ecommerce-Testing.postman_collection.json`

## Run Order (Recommended)
1. Register User
2. Login User
3. Create Product
4. Place Order
5. Get All Orders
6. Get Orders By User
7. Cancel Order (or Refund Order)
8. Delete Order

## Notes
- `baseUrl` default is `http://localhost:8080`
- `token`, `productId`, `orderId`, and `userId` are auto-updated from responses
- For cancel/refund, use the route format `/orders/{id}/cancel` and `/orders/{id}/refund`
- Start the app first: `./mvnw spring-boot:run` (PowerShell: `.\\mvnw spring-boot:run`)

Expected behavior:
- Without token, `/products/**` and `/orders/**` return 401 Unauthorized
- With valid token from `/users/login`, access is granted
