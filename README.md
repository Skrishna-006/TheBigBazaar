# ShopSphere

ShopSphere is a modular monolith e-commerce application scaffold built from scratch with a React + Vite frontend and a Spring Boot backend.

This repository currently contains the backend foundation from Phases 1 through 3:

- Project initialization
- Backend foundation
- PostgreSQL and JPA foundation
- Common backend architecture
- Frontend foundation
- Shared environment configuration
- A public health endpoint

Business modules such as authentication, products, cart, orders, payments, coupons, reviews, notifications, and analytics are **not implemented yet**.

## Technology Stack

- Frontend: React, Vite, JavaScript, React Router, Axios
- Backend: Java, Spring Boot, Spring Web, Spring Data JPA, Hibernate, Spring Security, Bean Validation, Maven
- Database: PostgreSQL

## Project Structure

- `backend/` Spring Boot application
- `frontend/` React + Vite application

## Backend Setup

Required environment variables:

- `DATABASE_URL`
- `DATABASE_USERNAME`
- `DATABASE_PASSWORD`
- `FRONTEND_URL`
- `JWT_SECRET`
- `JWT_ACCESS_EXPIRATION`
- `JWT_REFRESH_EXPIRATION`

Local defaults are provided in `backend/src/main/resources/application.yml`, but they are only development fallbacks. Production credentials must always be supplied through environment variables.

## PostgreSQL Setup

PostgreSQL must be installed and running locally before the backend can start.

Create the database named `shopsphere`:

Windows CLI:

```bash
createdb shopsphere
```

SQL alternative:

```sql
CREATE DATABASE shopsphere;
```

Recommended local database URL:

```bash
jdbc:postgresql://localhost:5432/shopsphere
```

Use environment variables to override credentials as needed:

- `DATABASE_URL`
- `DATABASE_USERNAME`
- `DATABASE_PASSWORD`

Do not assume the PostgreSQL username is always `postgres` in production.

Frontend development origin:

- `FRONTEND_URL`

Default:

- `http://localhost:5173`

JWT configuration:

- `JWT_SECRET` signs access and refresh tokens. Use a long random secret in production.
- `JWT_ACCESS_EXPIRATION` is the access-token lifetime in milliseconds.
- `JWT_REFRESH_EXPIRATION` is the refresh-token lifetime in milliseconds.

JWT settings:

- `JWT_SECRET` is the HMAC signing secret for access and refresh tokens
- `JWT_ACCESS_EXPIRATION` is the access token lifetime in milliseconds
- `JWT_REFRESH_EXPIRATION` is the refresh token lifetime in milliseconds

## Frontend Architecture

The frontend is built with React, Vite, React Router, and Axios.

- `src/api` holds centralized API clients.
- `src/components` stores reusable UI pieces such as layout and common status components.
- `src/features` will later hold feature-specific modules like auth, users, addresses, categories, brands, products, and inventory.
- `src/hooks` is reserved for reusable React hooks.
- `src/layouts` contains page layout shells like the main application layout.
- `src/pages` contains route-level pages.
- `src/routes` keeps route definitions organized.
- `src/utils` stores small helpers such as token storage and API error normalization.

Why this structure helps:

- API communication stays centralized instead of being duplicated across pages.
- Layouts can be reused across many pages.
- Feature modules keep future business logic isolated.
- Authentication will be added in Phase 11 without rewriting the whole app shell.

## Frontend Authentication

Frontend authentication is now integrated with the backend JWT system.

- `src/features/auth/api/authApi.js` contains the HTTP calls for register, login, refresh, logout, and current-user retrieval.
- `src/features/auth/context/AuthContext.jsx` stores the authenticated user and exposes `login()`, `register()`, `logout()`, `refreshAuthentication()`, `hasRole()`, and `isAdmin()`.
- `src/utils/tokenStorage.js` stores the access token and refresh token in browser localStorage for this learning project.
- `src/api/axiosClient.js` automatically attaches `Authorization: Bearer <token>` to authenticated requests and retries a request once after refresh when appropriate.
- `src/routes/ProtectedRoute.jsx` redirects unauthenticated users to `/login`.
- `src/routes/AdminRoute.jsx` prevents non-admin users from opening admin-only pages.

Authentication flow:

1. The user logs in or registers.
2. The backend validates credentials and returns JWT access and refresh tokens.
3. The frontend stores the tokens and keeps the current user in React state.
4. Axios adds the access token to protected requests automatically.
5. If the access token expires, the client attempts a refresh request using the refresh token.
6. If refresh succeeds, the request is retried with the new access token.
7. If refresh fails, the frontend clears local authentication state and sends the user back to a public route.

Storage note:

- This project uses localStorage for demo simplicity.
- That makes the tokens available to JavaScript, so it is not as strong as httpOnly secure cookies in a production security model.
- Passwords are never stored in browser storage.
- The frontend treats backend Spring Security as the real security boundary; frontend role checks are only for UI behavior.

## Product Catalog Frontend

The public catalog is built as a normal React experience that talks to the backend REST API.

- `src/features/products/api/productApi.js` fetches products and product details.
- `src/features/categories/api/categoryApi.js` fetches public category data.
- `src/features/brands/api/brandApi.js` fetches public brand data.
- `src/features/products/components/ProductCard.jsx` renders a single product tile.
- `src/features/products/components/ProductGrid.jsx` renders the responsive product list and empty state.
- `src/features/products/components/ProductFilters.jsx` renders category and brand filters.
- `src/pages/ProductDetailsPage.jsx` shows a public product detail page.

Catalog flow:

React UI -> Axios -> REST API -> Spring Boot -> PostgreSQL

Public browsing does not require authentication. If the user is logged in, Axios may still attach a token, but the catalog works for logged-out users as well.

Route choice:

- Product list: `/products`
- Product detail: `/products/:id`

The frontend uses the product ID for the detail URL to keep the first catalog implementation simple. The backend also supports slug-based product lookup, so a slug-based route can be added later if needed.

## User Profile Frontend

The authenticated user area is built around the existing backend profile API.

- `src/features/users/api/userApi.js` handles `GET /api/v1/users/me`, `PUT /api/v1/users/me`, and `PUT /api/v1/users/me/password`.
- `src/pages/ProfilePage.jsx` shows the current profile and includes forms for updating profile details and changing the password.
- The profile UI only edits fields that the backend allows, such as first name, last name, phone number, and avatar URL.

## Address Frontend

The address UI uses the authenticated backend address API.

- `src/features/addresses/api/addressApi.js` handles address retrieval and CRUD operations.
- `src/pages/AddressesPage.jsx` lists the user’s addresses and coordinates add, edit, delete, and set-default actions.
- `src/features/addresses/components/AddressCard.jsx` displays one address.
- `src/features/addresses/components/AddressForm.jsx` is reused for both create and edit mode.

React UI -> Axios -> Spring Boot REST API -> PostgreSQL

User and address ownership is determined by the authenticated backend user, not by any `userId` field sent from the browser.

## Cart Frontend

The cart UI uses the authenticated cart API.

- `src/features/cart/api/cartApi.js` calls the backend cart endpoints.
- `src/pages/CartPage.jsx` loads the user cart and coordinates add, update, remove, and clear actions.
- `src/features/cart/components/CartItemRow.jsx` renders one cart item with quantity controls.
- `src/features/cart/components/CartSummary.jsx` shows totals and clear-cart action.

Cart flow:

React UI -> Axios -> Spring Boot REST API -> PostgreSQL

Important cart rules:

- Cart belongs to the authenticated user.
- Cart does not reserve inventory.
- Stock is validated when cart quantities change.
- Final stock reservation will happen later during checkout/order creation.
- Cart uses the current `Product.price`.
- If product price changes, the cart reflects the current price rather than a historical snapshot.

## Wishlist Backend

The wishlist is another user-owned feature on the backend.

- One user can have many wishlist items.
- Each wishlist item belongs to exactly one user and exactly one product.
- Duplicate wishlist entries are rejected.
- Wishlist ownership comes from the authenticated JWT user, not from any `userId` sent by the browser.
- Wishlist items do not reserve inventory.
- Out-of-stock products can still be wishlisted.
- New wishlist entries are rejected if the product is inactive.
- The wishlist displays the current `Product.price` instead of storing a price snapshot.
- Existing wishlist items remain part of the user's wishlist even if a product later becomes inactive, and the response can mark the product as unavailable.
- Backend validation and security remain the source of truth; the frontend only reflects the authenticated user's data.

## Orders Backend

Orders capture a historical snapshot of what the customer purchased.

- A customer creates an order from the authenticated cart.
- The backend validates the selected shipping address belongs to that customer.
- Order items snapshot product name, SKU, image URL, unit price, and quantity so future catalog changes do not affect old orders.
- Shipping details are also snapshotted on the order so deleted or edited addresses do not change historical records.
- Order creation uses the backend price and inventory state, not values supplied by the frontend.
- Inventory is reduced when the order is created, and the stock change is recorded as an inventory movement.
- The cart is cleared only after the full order transaction succeeds.
- Customers can list and view only their own orders.
- Admin users can view and update order status across all users.
- Payment processing is not implemented yet. In this phase, creating the order represents successful placement before payment integration arrives later.

## Payments

ShopSphere uses a separate payment domain that sits on top of orders.

- `src/main/java/com/siva/shopsphere/payments/entity/Payment.java` stores the payment record.
- `PaymentStatus` uses explicit lifecycle states: `PENDING`, `AUTHORIZED`, `PAID`, `FAILED`, `CANCELLED`, and `REFUNDED`.
- `PaymentMethod` keeps the payment method extensible while staying explicit.
- `PaymentGateway` is the integration boundary for provider-specific behavior.
- `DevelopmentPaymentGateway` is the current provider implementation, and it is mock/development only.
- `PaymentService` owns payment creation, ownership checks, status transitions, and order synchronization.
- `PaymentController` exposes the authenticated payment APIs.

Payment architecture:

Order -> Payment -> PaymentGateway abstraction -> development/mock gateway

Important behavior:

- The backend always uses `Order.totalAmount` as the authoritative payment amount.
- Payment records never store card numbers, CVV, PINs, or other sensitive credentials.
- A customer can only access payments for their own order unless the backend role rules allow broader admin access.
- Because the current order flow already deducts inventory when the order is created, a failed payment cancels the order through the existing order flow instead of introducing a full inventory reservation rewrite.

Payment flow:

1. The customer places an order through the existing checkout flow.
2. The backend creates the order and deducts inventory.
3. The customer initiates payment for that order.
4. The development gateway returns a simulated success or failure result.
5. On success, the payment is marked `PAID` and the order moves forward.
6. On failure, the payment is marked `FAILED` and the order is cancelled through the existing order cancellation behavior.

Available endpoints:

- `POST /api/v1/payments/orders/{orderId}`
- `GET /api/v1/payments/{paymentId}`
- `GET /api/v1/orders/{orderId}/payment`

This phase does not integrate a real payment provider. A production gateway can be added later without changing the payment domain model.

## Payments Frontend

The payment UI is implemented in React and stays aligned with the backend payment contract.

- `src/features/payments/api/paymentApi.js` wraps the payment endpoints with the centralized Axios client.
- `src/pages/PaymentPage.jsx` loads the current order, shows the backend-authoritative total, and initiates a payment attempt.
- `src/features/payments/components/PaymentMethodSelector.jsx` lets the user choose a method without collecting sensitive credentials.
- `src/features/payments/components/PaymentStatus.jsx` renders payment lifecycle states consistently.
- `src/pages/PaymentSuccessPage.jsx` and `src/pages/PaymentFailurePage.jsx` show the result of a payment attempt.

Frontend payment routes:

- `/payment/:orderId`
- `/payment/success/:orderId`
- `/payment/failure/:orderId`

Important frontend behavior:

- Checkout creates the order first, then navigates to the payment page.
- The frontend never calculates the authoritative payment amount; it always displays `Order.totalAmount` from the backend.
- A payment attempt uses an idempotency key so accidental double submissions do not create duplicate payments.
- The frontend does not collect or store card numbers, CVV, UPI PINs, or other sensitive payment credentials.
- The UI works with the development/mock gateway only and does not imply real payment provider support.

## Checkout Frontend

The checkout flow is implemented in React and uses the existing cart and address APIs.

- `src/pages/CheckoutPage.jsx` loads the current cart and saved addresses.
- The user selects one shipping address and reviews the cart items and totals.
- `src/features/orders/api/orderApi.js` submits the selected `addressId` to the backend order API.
- The backend remains authoritative for totals, inventory checks, and order creation.

Checkout flow:

Cart -> Checkout -> Select Shipping Address -> Review Order -> Place Order -> Payment -> Order Details

## Orders Frontend

The customer order UI is built around the backend order APIs.

- `src/pages/OrdersPage.jsx` lists the authenticated user's orders.
- `src/pages/OrderDetailsPage.jsx` shows the historical snapshot for one order.
- `src/features/orders/components/OrderStatusBadge.jsx` displays the current order status.
- `src/features/orders/components/OrderItem.jsx` and `src/features/orders/components/OrderSummary.jsx` render the order detail view.
- `src/features/payments/components/PaymentStatus.jsx` shows the separate payment lifecycle where available.
- Cancellation uses the backend cancel endpoint when the current order status allows it.

Orders flow:

React UI -> Axios -> Spring Boot REST API -> PostgreSQL

The frontend always renders the backend order snapshot instead of reconstructing historical data from current products or addresses.

## Admin Frontend

The admin area is protected by the existing authentication flow plus role-based route guarding.

- `src/layouts/AdminLayout.jsx` provides the admin shell with navigation.
- `src/pages/admin/AdminDashboardPage.jsx` shows a lightweight operational dashboard from existing list endpoints.
- `src/pages/admin/AdminCategoriesPage.jsx` manages categories.
- `src/pages/admin/AdminBrandsPage.jsx` manages brands.
- `src/pages/admin/AdminProductsPage.jsx` manages products.
- `src/pages/admin/AdminInventoryPage.jsx` manages inventory and movement history.
- `src/pages/admin/AdminOrdersPage.jsx` and `src/pages/admin/AdminOrderDetailsPage.jsx` manage orders.

Admin routes:

- `/admin`
- `/admin/categories`
- `/admin/brands`
- `/admin/products`
- `/admin/inventory`
- `/admin/orders`
- `/admin/orders/:id`

Admin authorization remains enforced by Spring Security on the backend. The frontend route guard is only a usability layer.

### Start Backend

```bash
cd backend
mvn spring-boot:run
```

## Frontend Setup

Frontend uses a centralized Axios client configured through:

- `VITE_API_BASE_URL`

Example value:

```bash
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

### Start Frontend

```bash
cd frontend
npm install
npm run dev
```

## PostgreSQL Requirement

The backend is prepared for PostgreSQL and uses JPA/Hibernate with a development-safe schema strategy.

Phase 2 decisions:

- Hibernate schema mode: `update`
- Timestamp handling: UTC via `hibernate.jdbc.time_zone`
- Naming: snake_case via Spring Boot's default Hibernate naming strategy
- Identifier strategy: future business entities will generally use UUID identifiers

Production should later use database migrations such as Flyway.

## Backend Architecture

ShopSphere follows a simple layered backend architecture:

Controller
-> DTO
-> Service
-> Repository
-> PostgreSQL

Rules for future work:

- DTOs define API contracts.
- JPA entities are not exposed directly from controllers.
- Services contain business logic.
- Repositories handle persistence.
- Global exception handling returns consistent API errors.
- All REST endpoints use the `/api/v1` prefix.

Validation convention:

- Request DTOs should use Jakarta Bean Validation annotations such as `@NotBlank`, `@NotNull`, `@Email`, `@Size`, `@Positive`, `@PositiveOrZero`, and `@Pattern`.
- Controllers should use `@Valid` on request bodies where appropriate.

Mapper convention:

- Prefer small, manually written mappers for now.
- Do not introduce a generic base mapper or MapStruct yet unless there is a strong reason.

## Backend Architecture

The backend follows a layered REST architecture:

Controller -> DTO -> Service -> Repository -> PostgreSQL

Rules for future phases:

- Controllers handle HTTP concerns only
- DTOs define API contracts
- Services contain business logic
- Repositories handle persistence
- JPA entities are not exposed directly from controllers
- Global exception handling provides consistent API error responses
- All REST endpoints should live under the `/api/v1` prefix
- Request DTOs should use Jakarta Bean Validation annotations with `@Valid` in controllers
- Manual mappers are preferred initially for DTO and entity conversion
- Spring Security uses JWT access tokens plus refresh tokens for stateless authentication
- `CUSTOMER` and `ADMIN` are the domain roles, while Spring Security sees them as `ROLE_CUSTOMER` and `ROLE_ADMIN`

## Authentication and Profile

Authentication:

- Users can register and log in with email and password.
- Login returns JWT access and refresh tokens.
- JWT keeps the API stateless and is used for protected requests.

Profile:

- Authenticated users can view their own profile at `GET /api/v1/users/me`.
- Authenticated users can update their own profile at `PUT /api/v1/users/me`.
- Authenticated users can change their password at `PUT /api/v1/users/me/password`.

Security rules:

- Users cannot change their own role through profile endpoints.
- Users cannot change their account status through profile endpoints.
- Users cannot change their email through profile endpoints.
- Password changes require the current password and confirmation of the new password.

## Address Management

Each user can store multiple addresses.

- Addresses belong to exactly one user.
- Users can only create, view, update, and delete their own addresses.
- Only one address can be marked as the default at a time.
- Changing the default address is handled transactionally so the old default is cleared and the new default is set together.
- If the first address is created without explicitly marking it as default, the backend makes it the default automatically.
- If a default address is deleted and other addresses remain, the backend promotes another existing address to be the default.

## Catalog Foundation

Categories group products and brands identify the manufacturer or brand behind a product.

- Category and brand records are stored in PostgreSQL.
- Each record has a generated URL-friendly slug.
- Records can be deactivated instead of hard-deleted so future products can continue to reference them safely.
- Public list and detail endpoints return only active records by default.
- Category 1 -> N Product and Brand 1 -> N Product relationships will be added when Product is implemented in Phase 8.

## Product Module

Products belong to one category and one brand.

- Category 1 -> N Product
- Brand 1 -> N Product

Product details:

- `sku` is a unique admin-managed stock code.
- `slug` is generated from the product name for URL-friendly product pages.
- `price` uses `BigDecimal` so money is stored with decimal precision instead of floating-point values.
- Products are active by default and are normally deactivated instead of hard-deleted.
- Only ADMIN users can create, update, or deactivate products.

Inventory will be implemented separately in Phase 9, so product records do not represent the full stock-management system yet.

OpenAPI:

- Springdoc OpenAPI is included for basic API documentation support
- API metadata is configured with the title `ShopSphere API`, description `REST API for the ShopSphere e-commerce application`, and version `v1`

## Inventory Management

Inventory state is separate from the product catalog record.

- One product has one inventory record.
- Inventory tracks `quantity`, `reservedQuantity`, and `lowStockThreshold`.
- `availableQuantity = quantity - reservedQuantity`
- Low stock is calculated from available quantity and the threshold instead of being stored separately.
- Inventory changes are recorded as append-only movement history.
- `STOCK_IN`, `STOCK_OUT`, and `ADJUSTMENT` explain how stock changed.
- `reservedQuantity` is kept for future cart and order reservation logic.
- ADMIN users manage inventory; customers do not access these endpoints.
- Inventory updates are transactional so the stock change and movement record succeed or fail together.

## Current Implementation Status

### Implemented

- Backend Maven project
- Spring Boot application entry point
- Public health endpoint at `GET /api/v1/health`
- Frontend Vite app
- React Router routes for `/`, `/products`, `/login`, `/register`, and `/cart`
- Central Axios client
- Environment example file

### Not Yet Implemented

- Authentication
- JWT
- Wishlist logic
- Orders
- Payments
- Coupons
- Reviews
- Notifications
- Analytics

## Checkout Frontend

Checkout is a protected React flow that loads the authenticated user's cart and saved addresses, lets the user choose a shipping address, reviews the cart contents, and places the order by sending only `addressId` to the backend.

- The backend remains authoritative for prices, totals, and inventory validation.
- The frontend does not send subtotal, shipping, discount, or total values.
- When the cart is empty, checkout shows a friendly empty state and links back to products.
- When no address exists, checkout directs the user to the address management page.

## Orders Frontend

Orders are protected customer pages for browsing order history and viewing order snapshots.

- `/orders` lists the authenticated user's orders.
- `/orders/:id` shows order details, shipping snapshot, items, and totals.
- Cancellation is exposed only when the backend allows it.
- Order details always come from the backend snapshot response, not the current product or address tables.
