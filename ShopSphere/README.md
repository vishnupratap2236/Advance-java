# ShopSphere

A complete e-commerce platform built as a traditional Java web application:
**Jakarta Servlets + JSP/JSTL + JDBC (HikariCP) + MySQL**, deployed as a WAR on
**Apache Tomcat 11** and running on **Java 21**.

No Spring, no Node.js, no ORM - just Servlets, views, DAOs, services and SQL.

---

## 1. What it does

**Storefront**

* Home page with category tiles, featured products and new arrivals
* Catalogue with full text search, category filter, price range, in-stock filter,
  six sort orders and pagination
* Product detail page with image gallery, stock state, server side rating and reviews
* Shopping cart with quantity editing, removal, coupon codes and server side totals
* Checkout with address entry, coupon redemption and a clearly labelled demo payment
* Order confirmation, order history, order detail with a status timeline
* Customer account: profile, password change, address book, wishlist, product reviews

**Administration** (role protected)

* Dashboard with revenue, order, product, customer and inventory figures
* Product management: create, edit, activate/deactivate, delete, stock correction,
  image upload
* Category management
* Order management: filter by status or customer, change the status, cancel a customer
  order (the reserved stock is returned)
* Customer management: list accounts, suspend and reactivate

**REST endpoints**

```
GET  /api/products?q=&category=&sort=&page=&pageSize=   public catalogue (no personal data)
GET  /api/cart                    session required - the cart of the signed in customer
POST /api/cart  productId=&quantity=   session required, CSRF header required
```

Access policy: `/api/products` is a deliberately public read-only catalogue endpoint.
Every endpoint that returns personal data (`/api/cart`, `/account`, `/orders`, `/order`,
`/cart`, `/checkout`, `/wishlist`) requires an authenticated session, and `/admin/**`
requires the `ADMIN` role. Anonymous API calls answer with `401`, anonymous page
requests are redirected to the login page.

---

## 2. Architecture

```
src/main/java/com/shopsphere
├── config/AppConfig.java          configuration resolution (system property > env var > file > default)
├── db/Database.java               HikariCP pool, single entry point for connections
├── db/MigrationRunner.java        versioned SQL migrations, applied once and tracked
├── db/Transactions.java           transaction helper (commit / rollback)
├── model/                         entities: User, Product, Category, CartItem, Order, OrderItem,
│                                   Payment, Review, Coupon, Address, DashboardStats, PageResult
├── dao/                           one class per table, prepared statements, try-with-resources
├── service/                       business rules: Auth, Catalog, Cart, Order, Address,
│                                   Review, Wishlist, Admin
├── inventory/                     inventory service with a local, a socket and an RMI transport
├── util/                          PasswordHasher (BCrypt), Csrf, Validation, Money, WebContext,
│                                   ImageStorage, Json
└── web/
    ├── BaseServlet.java           view rendering, redirects, safe error handling
    ├── AppContextListener.java    start-up: migrations, bootstrap admin, pool shutdown
    ├── filter/                    encoding, CSRF, authentication, admin role, no-cache
    ├── customer/                  home, catalogue, product, cart, checkout, orders, account,
    │                               addresses, wishlist, review, login, register, logout
    ├── admin/                     dashboard, products, categories, orders, customers, upload
    └── api/                       product and cart JSON endpoints

src/main/webapp
├── WEB-INF/web.xml                filter chain, session configuration, error pages
├── WEB-INF/views/                 JSP views (layout fragments, storefront, admin)
├── css/app.css                    custom theme on top of Bootstrap 5
├── js/app.js                      quantity steppers, gallery, AJAX add-to-cart, uploader
├── images/                        locally generated SVG artwork (no remote assets)
└── vendor/bootstrap/              Bootstrap 5.3.3 served locally
```

Request flow: `Filter -> Servlet -> Service -> DAO -> MySQL`, views are JSP with JSTL.
All money is `DECIMAL` in the database and `BigDecimal` in Java, all SQL uses prepared
statements.

---

## 3. Database

Schema (MySQL 8.4+ / 26.x, InnoDB, utf8mb4):

| Table | Purpose |
|---|---|
| `users` | accounts, BCrypt hash, role (`CUSTOMER` / `ADMIN`), status |
| `categories` | catalogue categories with a unique slug |
| `products` | catalogue items, price, stock, flags, denormalised rating |
| `product_images` | gallery images per product |
| `addresses` | address book of a customer |
| `carts` / `cart_items` | one cart per customer, one line per product |
| `coupons` | discount rules, global and per customer usage limits |
| `coupon_usage` | which customer used which coupon for which order |
| `orders` | order header with a snapshot of the delivery address and the totals |
| `order_items` | order lines with a snapshot of the product name and price |
| `order_status_history` | status timeline of an order |
| `payments` | payment record per order (demo gateway response) |
| `reviews` | customer reviews, one per customer and product |
| `wishlist_items` | saved products of a customer |
| `login_attempts` | login throttling |
| `schema_migrations` | applied migration versions |

Foreign keys, unique constraints, check constraints and indexes are defined in the
migration scripts. Migrations live in `src/main/resources/db/migrations` and are applied
automatically at start-up by `MigrationRunner`; they are applied at most once and are
tracked in `schema_migrations`.

### Least privilege

The application never runs as `root`. The account `shopsphere_app` may only work inside
the `shopsphere` schema:

```sql
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, REFERENCES, DROP
    ON shopsphere.* TO 'shopsphere_app'@'localhost';
```

Create the database and the account with `db\setup-database.cmd` (asks for the
administrator password interactively) or with `db/setup-database.sql` after replacing
the password placeholder.

---

## 4. Configuration

No secret is stored in the repository. Every setting is resolved in this order:

1. JVM system property, e.g. `-Dshopsphere.db.url=...`
2. environment variable, e.g. `DB_URL`, `DB_USER`, `DB_PASSWORD`
3. external properties file pointed at by `SHOPSPHERE_CONFIG` or
   `-Dshopsphere.config=/path/to/db.properties`
4. built-in default

`config/db.example.properties` documents every option; the real file
`config/db.properties` is git-ignored. For the Tomcat deployment the file is referenced
from `C:\apache-tomcat-11.0.26\bin\setenv.bat`.

---

## 5. Build, test, run

```bash
# build + unit tests + integration tests + WAR
mvn clean verify
# -> target/shopsphere.war

# deploy
copy target\shopsphere.war C:\apache-tomcat-11.0.26\webapps\
C:\apache-tomcat-11.0.26\bin\startup.bat
```

Open <http://localhost:8080/shopsphere/>.

### Tests

* `mvn test` - 28 unit tests (BCrypt, validation, CSRF, migrations, model rules, socket inventory)
* `mvn verify` - 49 integration tests against the real database (schema, registration,
  login and throttling, catalogue queries, cart arithmetic, checkout transactions,
  oversell protection, ownership checks, coupons, reviews, wishlist, payments,
  administration CRUD)

The integration tests skip themselves automatically when no database is reachable.

### End-to-end HTTP check

`tools/smoke-test.ps1` drives the deployed application over HTTP: registration, cart,
checkout, order history, review, wishlist, the administration status update and the
security checks (anonymous admin access, missing CSRF token, wrong password).

```powershell
powershell -ExecutionPolicy Bypass -File tools\smoke-test.ps1 `
    -AdminEmail admin@shopsphere.local -AdminPassword <password>
```

### Creating the first administrator

```bash
mvn -q dependency:build-classpath "-Dmdep.outputFile=target\cp.txt"
java "-Dshopsphere.config=D:\Java projects\ShopSphere\config\db.properties" ^
     -cp "target\classes;$(cat target\cp.txt)" ^
     com.shopsphere.tools.AdminUserCreator admin@shopsphere.local <password>
```

Alternatively set `SHOPSPHERE_ADMIN_PASSWORD` before the first start; the application
then creates the administrator itself. There is no built-in default password.

---

## 6. Security measures

* BCrypt password hashing (work factor 12, random salt), rehashing of weak hashes on login
* Session based authentication, session id renewed after login, session invalidated on logout
* Central filters: authentication for the customer area, role check for `/admin`
* CSRF token per session for every state changing request (constant time comparison)
* Server side validation of every input; the browser validation is only a convenience
* Prepared statements everywhere, no string concatenation of user input
* `HttpOnly` session cookie, `X-Content-Type-Options`, no-store caching for personal pages
* Login throttling: five failed attempts lock the account for fifteen minutes
* Ownership checks on orders, addresses, reviews and the wishlist (IDOR protection)
* Stock is decremented with a conditional update inside the checkout transaction, so a
  product can never be oversold; a failing step rolls the whole order back
* Uploaded images are validated by magic bytes, size limited and stored outside the
  deployed application; the file name is generated by the server
* Error pages never expose stack traces, SQL or credentials
* The application account only has rights on its own schema

---

## 7. Demo payment and e-mail

There is no payment gateway and no mail server in this environment. The checkout
therefore offers **cash on delivery**, a clearly labelled **demo card payment** and a
**demo bank transfer**. A payment row is written for every order with a
`payment_gateway_response` that states plainly that no provider was contacted. The
confirmation screen tells the customer that no money was moved.

Order confirmation e-mails are not sent. The confirmation is shown in the browser and
the order stays available in the order history.

---

## 8. Reference material

`reference-only/` contains the original deployment that was found in
`C:\apache-tomcat-11.0.26\webapps` (WAR and exploded application) together with the
original `pom.xml`. It is kept for comparison only and is not part of the build.
The entities of that implementation (payments, coupon usage, reviews, wishlist and the
RMI / socket inventory service) are part of this implementation as well.

---

## 9. Notes and limitations

* Timestamps are stored in UTC and rendered in the configured display zone
  (`timezone` in the configuration file).
* A coupon can be used once per customer by default (`usage_per_user`).
* The cart holds at most 20 units of a product and never more than the available stock.
* The image upload directory can be changed with `SHOPSPHERE_UPLOAD_DIR`.
* The inventory service runs in `local` mode by default; `socket` and `rmi` are
  available for a distributed setup. The database transaction always stays the
  authority for the stock.
