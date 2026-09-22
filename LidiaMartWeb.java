import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.awt.Desktop;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.Executors;

public class LidiaMartWeb {

    // ================= DATABASE =================

    static final String DB_URL =
            "jdbc:mysql://localhost:3306/lidia_mart";
    static final String DB_USER = "root";

    // IMPORTANT:
    // Replace this with your MySQL root password.
    static final String DB_PASSWORD = "BrijithLidia20122007";

    static final String IMAGE_FOLDER = "images";

    static Map<String, String> sessions = new HashMap<>();

    // ================= MAIN =================

    public static void main(String[] args) throws Exception {

        Class.forName("com.mysql.cj.jdbc.Driver");

        try (Connection con = getConnection()) {
            System.out.println("====================================");
            System.out.println("       LIDIA MART");
            System.out.println("====================================");
            System.out.println("MySQL Connection Successful!");

            setupDatabase(con);
        }

        HttpServer server = HttpServer.create(
                new InetSocketAddress(8080), 0);

        server.createContext("/", LidiaMartWeb::homePage);
        server.createContext("/login", LidiaMartWeb::login);
        server.createContext("/register", LidiaMartWeb::register);
        server.createContext("/dashboard", LidiaMartWeb::dashboard);
        server.createContext("/logout", LidiaMartWeb::logout);

        server.createContext("/cart", LidiaMartWeb::cart);
        server.createContext("/add-cart", LidiaMartWeb::addCart);
        server.createContext("/increase-cart", LidiaMartWeb::increaseCart);
        server.createContext("/decrease-cart", LidiaMartWeb::decreaseCart);
        server.createContext("/remove-cart", LidiaMartWeb::removeCart);

        server.createContext("/wishlist", LidiaMartWeb::wishlist);
        server.createContext("/add-wishlist", LidiaMartWeb::addWishlist);
        server.createContext("/remove-wishlist", LidiaMartWeb::removeWishlist);

        server.createContext("/checkout", LidiaMartWeb::checkout);

        server.createContext("/new-product", LidiaMartWeb::newProduct);
        server.createContext("/remove-product", LidiaMartWeb::removeProduct);

        server.createContext("/images", LidiaMartWeb::images);

        server.setExecutor(
                Executors.newCachedThreadPool());

        server.start();

        System.out.println("Server started!");
        System.out.println(
                "Open: http://localhost:8080");

        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(
                    new URI("http://localhost:8080"));
        }
    }

    // ================= DATABASE CONNECTION =================

    static Connection getConnection()
            throws SQLException {

        return DriverManager.getConnection(
                DB_URL,
                DB_USER,
                DB_PASSWORD);
    }

    // ================= DATABASE SETUP =================

    static void setupDatabase(Connection con)
            throws SQLException {

        // Add role column if it does not already exist
        try {
            Statement st = con.createStatement();

            st.executeUpdate(
                    "ALTER TABLE users ADD COLUMN role VARCHAR(20) DEFAULT 'Buyer'");

        } catch (SQLException e) {
            // Column already exists
        }

        // Add image column if it does not already exist
        try {
            Statement st = con.createStatement();

            st.executeUpdate(
                    "ALTER TABLE products ADD COLUMN image VARCHAR(255)");

        } catch (SQLException e) {
            // Column already exists
        }

        // Admin account
        String checkAdmin =
                "SELECT id FROM users WHERE username=?";

        try (PreparedStatement ps =
                     con.prepareStatement(checkAdmin)) {

            ps.setString(1, "admin");

            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {

                String sql =
                        "INSERT INTO users " +
                        "(username,email,password,role) " +
                        "VALUES (?,?,?,?)";

                try (PreparedStatement insert =
                             con.prepareStatement(sql)) {

                    insert.setString(
                            1, "admin");

                    insert.setString(
                            2, "admin@lidiamart.com");

                    insert.setString(
                            3, "admin123");

                    insert.setString(
                            4, "Admin");

                    insert.executeUpdate();
                }
            }
        }

        // Add default products
        addProductIfMissing(
                con,
                "Gold Necklace",
                "Necklace",
                500,
                10,
                "necklace.webp");

        addProductIfMissing(
                con,
                "Bracelet",
                "Bracelet",
                300,
                10,
                "bracelet.webp");

        addProductIfMissing(
                con,
                "Earrings",
                "Earrings",
                250,
                10,
                "earrings.jpeg");

        addProductIfMissing(
                con,
                "Handbag",
                "Handbag",
                800,
                10,
                "handbags.png");
    }

    static void addProductIfMissing(
            Connection con,
            String name,
            String category,
            double price,
            int stock,
            String image)
            throws SQLException {

        String check =
                "SELECT id FROM products WHERE name=?";

        try (PreparedStatement ps =
                     con.prepareStatement(check)) {

            ps.setString(1, name);

            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {

                String sql =
                        "INSERT INTO products " +
                        "(name,category,price,stock,image) " +
                        "VALUES (?,?,?,?,?)";

                try (PreparedStatement insert =
                             con.prepareStatement(sql)) {

                    insert.setString(1, name);
                    insert.setString(2, category);
                    insert.setDouble(3, price);
                    insert.setInt(4, stock);
                    insert.setString(5, image);

                    insert.executeUpdate();
                }

            } else {

                String sql =
                        "UPDATE products SET image=? " +
                        "WHERE name=?";

                try (PreparedStatement update =
                             con.prepareStatement(sql)) {

                    update.setString(1, image);
                    update.setString(2, name);

                    update.executeUpdate();
                }
            }
        }
    }

    // ================= HOME PAGE =================

    static void homePage(
            HttpExchange exchange)
            throws IOException {

        String html = """
                <html>
                <head>
                <title>LidiaMart Login</title>

                <style>

                body {
                    font-family: Arial;
                    background: #f4f4f4;
                    margin: 0;
                }

                .header {
                    background: #222;
                    color: white;
                    padding: 25px;
                    text-align: center;
                }

                .box {
                    width: 360px;
                    margin: 60px auto;
                    background: white;
                    padding: 30px;
                    border-radius: 12px;
                    box-shadow: 0 0 15px #aaa;
                }

                input, button {
                    width: 100%;
                    padding: 12px;
                    margin: 8px 0;
                    box-sizing: border-box;
                }

                button {
                    background: #222;
                    color: white;
                    border: none;
                    cursor: pointer;
                }

                a {
                    text-decoration: none;
                }

                </style>
                </head>

                <body>

                <div class="header">
                    <h1>LIDIA MART</h1>
                    <p>Accessories Online Shopping</p>
                </div>

                <div class="box">

                    <h2>Login</h2>

                    <form action="/login" method="post">

                        <input
                            name="username"
                            placeholder="Username"
                            required>

                        <input
                            type="password"
                            name="password"
                            placeholder="Password"
                            required>

                        <button type="submit">
                            LOGIN
                        </button>

                    </form>

                    <p style="text-align:center">
                        <a href="/register">
                            New User? Register Here
                        </a>
                    </p>

                </div>

                </body>
                </html>
                """;

        sendResponse(exchange, html);
    }

    // ================= LOGIN =================

    static void login(
            HttpExchange exchange)
            throws IOException {

        if (!exchange.getRequestMethod()
                .equalsIgnoreCase("POST")) {

            redirect(exchange, "/");
            return;
        }

        Map<String, String> form =
                parseForm(readRequest(exchange));

        String username =
                form.get("username");

        String password =
                form.get("password");

        try (Connection con =
                     getConnection()) {

            String sql =
                    "SELECT id FROM users " +
                    "WHERE username=? AND password=?";

            try (PreparedStatement ps =
                         con.prepareStatement(sql)) {

                ps.setString(1, username);
                ps.setString(2, password);

                ResultSet rs =
                        ps.executeQuery();

                if (rs.next()) {

                    String session =
                            UUID.randomUUID()
                                    .toString();

                    sessions.put(
                            session,
                            username);

                    exchange.getResponseHeaders()
                            .add(
                                    "Set-Cookie",
                                    "SESSION=" +
                                    session +
                                    "; Path=/");

                    redirect(
                            exchange,
                            "/dashboard");

                } else {

                    sendResponse(
                            exchange,
                            "<h2 style='text-align:center'>" +
                            "Invalid Username or Password!" +
                            "<br><br>" +
                            "<a href='/'>Back</a>" +
                            "</h2>");
                }
            }

        } catch (SQLException e) {

            sendResponse(
                    exchange,
                    "<h2>MySQL Error</h2>" +
                    e.getMessage());
        }
    }

    // ================= REGISTER =================

    static void register(
            HttpExchange exchange)
            throws IOException {

        if (exchange.getRequestMethod()
                .equalsIgnoreCase("GET")) {

            String html = """
                    <html>
                    <head>
                    <title>Register</title>

                    <style>

                    body {
                        font-family: Arial;
                        background: #f4f4f4;
                    }

                    .box {
                        width: 400px;
                        margin: 50px auto;
                        background: white;
                        padding: 30px;
                        border-radius: 12px;
                    }

                    input, select, button {
                        width: 100%;
                        padding: 12px;
                        margin: 8px 0;
                        box-sizing: border-box;
                    }

                    button {
                        background: #222;
                        color: white;
                        border: none;
                    }

                    </style>
                    </head>

                    <body>

                    <div class="box">

                    <h2>Create Account</h2>

                    <form action="/register"
                          method="post">

                    <input name="username"
                           placeholder="Username"
                           required>

                    <input name="email"
                           type="email"
                           placeholder="Email"
                           required>

                    <input name="password"
                           type="password"
                           placeholder="Password"
                           required>

                    <select name="role">

                        <option value="Buyer">
                            Buyer
                        </option>

                        <option value="Seller">
                            Seller
                        </option>

                    </select>

                    <button type="submit">
                        REGISTER
                    </button>

                    </form>

                    <a href="/">
                        Already have an account?
                    </a>

                    </div>

                    </body>
                    </html>
                    """;

            sendResponse(exchange, html);
            return;
        }

        Map<String, String> form =
                parseForm(readRequest(exchange));

        String username =
                form.get("username");

        String email =
                form.get("email");

        String password =
                form.get("password");

        String role =
                form.get("role");

        try (Connection con =
                     getConnection()) {

            String check =
                    "SELECT id FROM users " +
                    "WHERE username=?";

            try (PreparedStatement ps =
                         con.prepareStatement(check)) {

                ps.setString(1, username);

                ResultSet rs =
                        ps.executeQuery();

                if (rs.next()) {

                    sendResponse(
                            exchange,
                            "<h2>Username already exists!</h2>" +
                            "<a href='/register'>Try Again</a>");

                    return;
                }
            }

            String sql =
                    "INSERT INTO users " +
                    "(username,email,password,role) " +
                    "VALUES (?,?,?,?)";

            try (PreparedStatement ps =
                         con.prepareStatement(sql)) {

                ps.setString(1, username);
                ps.setString(2, email);
                ps.setString(3, password);
                ps.setString(4, role);

                ps.executeUpdate();
            }

            sendResponse(
                    exchange,
                    "<h1 style='text-align:center'>" +
                    "Registration Successful!" +
                    "</h1>" +
                    "<p style='text-align:center'>" +
                    "<a href='/'>Go to Login</a>" +
                    "</p>");

        } catch (SQLException e) {

            sendResponse(
                    exchange,
                    "<h2>Registration Error</h2>" +
                    e.getMessage());
        }
    }

    // ================= DASHBOARD =================

    static void dashboard(
            HttpExchange exchange)
            throws IOException {

        String username =
                getLoggedUser(exchange);

        if (username == null) {
            redirect(exchange, "/");
            return;
        }

        try (Connection con =
                     getConnection()) {

            String sql =
                    "SELECT role FROM users " +
                    "WHERE username=?";

            try (PreparedStatement ps =
                         con.prepareStatement(sql)) {

                ps.setString(1, username);

                ResultSet rs =
                        ps.executeQuery();

                if (!rs.next()) {
                    redirect(exchange, "/");
                    return;
                }

                String role =
                        rs.getString("role");

                if ("Admin".equalsIgnoreCase(role)) {

                    adminDashboard(exchange);

                } else {

                    buyerDashboard(
                            exchange,
                            username);
                }
            }

        } catch (SQLException e) {

            sendResponse(
                    exchange,
                    "<h2>Database Error</h2>" +
                    e.getMessage());
        }
    }

    // ================= BUYER DASHBOARD =================

    static void buyerDashboard(
            HttpExchange exchange,
            String username)
            throws IOException {

        StringBuilder products =
                new StringBuilder();

        try (Connection con =
                     getConnection()) {

            String sql =
                    "SELECT * FROM products";

            Statement st =
                    con.createStatement();

            ResultSet rs =
                    st.executeQuery(sql);

            while (rs.next()) {

                int id =
                        rs.getInt("id");

                String name =
                        rs.getString("name");

                String category =
                        rs.getString("category");

                double price =
                        rs.getDouble("price");

                int stock =
                        rs.getInt("stock");

                String image =
                        rs.getString("image");

                if (image == null)
                    image = "";

                products.append("""

                        <div class="product">

                            <img src="/images/IMAGE"
                                 onerror="this.style.display='none'">

                            <h3>NAME</h3>

                            <p>Category: CATEGORY</p>

                            <p class="price">
                                Rs.PRICE
                            </p>

                            <p>
                                Stock: STOCK
                            </p>

                            <form action="/add-cart"
                                  method="post">

                                <input type="hidden"
                                       name="product_id"
                                       value="ID">

                                <button type="submit">
                                    Add to Cart
                                </button>

                            </form>

                            <form action="/add-wishlist"
                                  method="post">

                                <input type="hidden"
                                       name="product_id"
                                       value="ID">

                                <button type="submit"
                                        class="wish">
                                    ♡ Wishlist
                                </button>

                            </form>

                        </div>

                        """
                        .replace("IMAGE",
                                htmlEscape(image))
                        .replace("NAME",
                                htmlEscape(name))
                        .replace("CATEGORY",
                                htmlEscape(category))
                        .replace("PRICE",
                                String.format("%.2f", price))
                        .replace("STOCK",
                                String.valueOf(stock))
                        .replace("ID",
                                String.valueOf(id)));
            }

        } catch (SQLException e) {

            products.append(
                    "<p>Database error: " +
                    e.getMessage() +
                    "</p>");
        }

        String html = """
                <html>

                <head>

                <title>LidiaMart Buyer Home</title>

                <style>

                body {
                    font-family: Arial;
                    margin: 0;
                    background: #f4f4f4;
                }

                .header {
                    background: #222;
                    color: white;
                    padding: 20px;
                    text-align: center;
                }

                .nav {
                    background: white;
                    padding: 15px;
                    text-align: center;
                }

                .nav a {
                    margin: 10px;
                    text-decoration: none;
                    font-weight: bold;
                }

                .products {
                    display: grid;
                    grid-template-columns:
                        repeat(auto-fit,minmax(220px,1fr));
                    gap: 20px;
                    padding: 30px;
                }

                .product {
                    background: white;
                    padding: 20px;
                    border-radius: 12px;
                    text-align: center;
                    box-shadow: 0 0 8px #ccc;
                }

                .product img {
                    width: 180px;
                    height: 180px;
                    object-fit: contain;
                }

                .price {
                    font-size: 20px;
                    font-weight: bold;
                }

                button {
                    width: 100%;
                    padding: 10px;
                    margin-top: 8px;
                    background: #222;
                    color: white;
                    border: none;
                    cursor: pointer;
                }

                .wish {
                    background: #a00000;
                }

                </style>

                </head>

                <body>

                <div class="header">

                    <h1>LIDIA MART</h1>

                    <p>
                        Welcome, USER
                    </p>

                </div>

                <div class="nav">

                    <a href="/dashboard">
                        Products
                    </a>

                    <a href="/cart">
                        🛒 Cart
                    </a>

                    <a href="/wishlist">
                        ❤️ Wishlist
                    </a>

                    <a href="/logout">
                        Logout
                    </a>

                </div>

                <div class="products">
                    PRODUCTS
                </div>

                </body>
                </html>
                """
                .replace("USER",
                        htmlEscape(username))
                .replace("PRODUCTS",
                        products.toString());

        sendResponse(exchange, html);
    }

    // ================= ADD CART =================

    static void addCart(
            HttpExchange exchange)
            throws IOException {

        Map<String, String> form =
                parseForm(readRequest(exchange));

        String username =
                getLoggedUser(exchange);

        if (username == null) {
            redirect(exchange, "/");
            return;
        }

        int productId =
                Integer.parseInt(
                        form.get("product_id"));

        try (Connection con =
                     getConnection()) {

            int userId =
                    getUserId(con, username);

            String check =
                    "SELECT id,quantity " +
                    "FROM cart " +
                    "WHERE user_id=? AND product_id=?";

            try (PreparedStatement ps =
                         con.prepareStatement(check)) {

                ps.setInt(1, userId);
                ps.setInt(2, productId);

                ResultSet rs =
                        ps.executeQuery();

                if (rs.next()) {

                    String update =
                            "UPDATE cart SET quantity=" +
                            "quantity+1 WHERE id=?";

                    try (PreparedStatement up =
                                 con.prepareStatement(update)) {

                        up.setInt(
                                1,
                                rs.getInt("id"));

                        up.executeUpdate();
                    }

                } else {

                    String insert =
                            "INSERT INTO cart " +
                            "(user_id,product_id,quantity) " +
                            "VALUES (?,?,1)";

                    try (PreparedStatement ins =
                                 con.prepareStatement(insert)) {

                        ins.setInt(1, userId);
                        ins.setInt(2, productId);

                        ins.executeUpdate();
                    }
                }
            }

            redirect(exchange, "/cart");

        } catch (SQLException e) {

            sendResponse(
                    exchange,
                    "<h2>Cart Error</h2>" +
                    e.getMessage());
        }
    }

    // ================= CART =================

    static void cart(
            HttpExchange exchange)
            throws IOException {

        String username =
                getLoggedUser(exchange);

        if (username == null) {
            redirect(exchange, "/");
            return;
        }

        StringBuilder rows =
                new StringBuilder();

        double total = 0;

        try (Connection con =
                     getConnection()) {

            int userId =
                    getUserId(con, username);

            String sql =
                    """
                    SELECT c.id,
                           c.product_id,
                           c.quantity,
                           p.name,
                           p.price
                    FROM cart c
                    JOIN products p
                    ON c.product_id=p.id
                    WHERE c.user_id=?
                    """;

            try (PreparedStatement ps =
                         con.prepareStatement(sql)) {

                ps.setInt(1, userId);

                ResultSet rs =
                        ps.executeQuery();

                while (rs.next()) {

                    int cartId =
                            rs.getInt("id");

                    int productId =
                            rs.getInt("product_id");

                    String name =
                            rs.getString("name");

                    double price =
                            rs.getDouble("price");

                    int quantity =
                            rs.getInt("quantity");

                    double subtotal =
                            price * quantity;

                    total += subtotal;

                    rows.append("""
                            <div class="cartRow">

                            <h3>NAME</h3>

                            <p>
                                Price: Rs.PRICE
                            </p>

                            <p>
                                Quantity:
                            </p>

                            <form action="/decrease-cart"
                                  method="post"
                                  style="display:inline">

                                <input type="hidden"
                                       name="cart_id"
                                       value="CARTID">

                                <button>-</button>

                            </form>

                            <b> QTY </b>

                            <form action="/increase-cart"
                                  method="post"
                                  style="display:inline">

                                <input type="hidden"
                                       name="cart_id"
                                       value="CARTID">

                                <button>+</button>

                            </form>

                            <p>
                                Subtotal:
                                Rs.SUBTOTAL
                            </p>

                            <form action="/remove-cart"
                                  method="post">

                                <input type="hidden"
                                       name="cart_id"
                                       value="CARTID">

                                <button class="remove">
                                    Remove
                                </button>

                            </form>

                            </div>
                            """
                            .replace("NAME",
                                    htmlEscape(name))
                            .replace("PRICE",
                                    String.format(
                                            "%.2f",
                                            price))
                            .replace("QTY",
                                    String.valueOf(
                                            quantity))
                            .replace("SUBTOTAL",
                                    String.format(
                                            "%.2f",
                                            subtotal))
                            .replace("CARTID",
                                    String.valueOf(
                                            cartId)));
                }
            }

        } catch (SQLException e) {

            rows.append(
                    "<p>" +
                    e.getMessage() +
                    "</p>");
        }

        String html = """
                <html>

                <head>

                <title>Shopping Cart</title>

                <style>

                body {
                    font-family: Arial;
                    background: #f4f4f4;
                    padding: 30px;
                }

                .cartRow {
                    background: white;
                    padding: 20px;
                    margin: 15px auto;
                    max-width: 600px;
                    border-radius: 10px;
                }

                button {
                    padding: 8px 15px;
                    margin: 5px;
                    background: #222;
                    color: white;
                    border: none;
                }

                .remove {
                    background: #b00000;
                }

                .checkout {
                    background: green;
                    padding: 12px 25px;
                }

                </style>

                </head>

                <body>

                <h1>🛒 My Cart</h1>

                ROWS

                <h2>
                    Total: Rs.TOTAL
                </h2>

                <form action="/checkout"
                      method="post">

                    <button class="checkout">
                        PLACE ORDER
                    </button>

                </form>

                <br>

                <a href="/dashboard">
                    Continue Shopping
                </a>

                </body>
                </html>
                """
                .replace("ROWS",
                        rows.toString())
                .replace("TOTAL",
                        String.format(
                                "%.2f",
                                total));

        sendResponse(exchange, html);
    }

    // ================= INCREASE CART =================

    static void increaseCart(
            HttpExchange exchange)
            throws IOException {

        changeQuantity(
                exchange,
                "quantity+1");
    }

    // ================= DECREASE CART =================

    static void decreaseCart(
            HttpExchange exchange)
            throws IOException {

        changeQuantity(
                exchange,
                "quantity-1");
    }

    static void changeQuantity(
            HttpExchange exchange,
            String operation)
            throws IOException {

        Map<String, String> form =
                parseForm(readRequest(exchange));

        int cartId =
                Integer.parseInt(
                        form.get("cart_id"));

        String username =
                getLoggedUser(exchange);

        if (username == null) {
            redirect(exchange, "/");
            return;
        }

        try (Connection con =
                     getConnection()) {

            int userId =
                    getUserId(con, username);

            String sql =
                    "UPDATE cart SET quantity=" +
                    operation +
                    " WHERE id=? AND user_id=?";

            try (PreparedStatement ps =
                         con.prepareStatement(sql)) {

                ps.setInt(1, cartId);
                ps.setInt(2, userId);

                ps.executeUpdate();
            }

            // Delete zero quantity
            try (PreparedStatement ps =
                         con.prepareStatement(
                                 "DELETE FROM cart " +
                                 "WHERE id=? AND quantity<=0")) {

                ps.setInt(1, cartId);
                ps.executeUpdate();
            }

            redirect(exchange, "/cart");

        } catch (SQLException e) {

            sendResponse(
                    exchange,
                    "<h2>" +
                    e.getMessage() +
                    "</h2>");
        }
    }

    // ================= REMOVE CART =================

    static void removeCart(
            HttpExchange exchange)
            throws IOException {

        Map<String, String> form =
                parseForm(readRequest(exchange));

        int cartId =
                Integer.parseInt(
                        form.get("cart_id"));

        String username =
                getLoggedUser(exchange);

        if (username == null) {
            redirect(exchange, "/");
            return;
        }

        try (Connection con =
                     getConnection()) {

            int userId =
                    getUserId(con, username);

            String sql =
                    "DELETE FROM cart " +
                    "WHERE id=? AND user_id=?";

            try (PreparedStatement ps =
                         con.prepareStatement(sql)) {

                ps.setInt(1, cartId);
                ps.setInt(2, userId);

                ps.executeUpdate();
            }

            redirect(exchange, "/cart");

        } catch (SQLException e) {

            sendResponse(
                    exchange,
                    "<h2>" +
                    e.getMessage() +
                    "</h2>");
        }
    }

    // ================= WISHLIST =================

    static void addWishlist(
            HttpExchange exchange)
            throws IOException {

        Map<String, String> form =
                parseForm(readRequest(exchange));

        String username =
                getLoggedUser(exchange);

        if (username == null) {
            redirect(exchange, "/");
            return;
        }

        int productId =
                Integer.parseInt(
                        form.get("product_id"));

        try (Connection con =
                     getConnection()) {

            int userId =
                    getUserId(con, username);

            String check =
                    "SELECT id FROM wishlist " +
                    "WHERE user_id=? AND product_id=?";

            try (PreparedStatement ps =
                         con.prepareStatement(check)) {

                ps.setInt(1, userId);
                ps.setInt(2, productId);

                ResultSet rs =
                        ps.executeQuery();

                if (!rs.next()) {

                    String insert =
                            "INSERT INTO wishlist " +
                            "(user_id,product_id) " +
                            "VALUES (?,?)";

                    try (PreparedStatement ins =
                                 con.prepareStatement(insert)) {

                        ins.setInt(1, userId);
                        ins.setInt(2, productId);

                        ins.executeUpdate();
                    }
                }
            }

            redirect(
                    exchange,
                    "/wishlist");

        } catch (SQLException e) {

            sendResponse(
                    exchange,
                    "<h2>" +
                    e.getMessage() +
                    "</h2>");
        }
    }

    static void wishlist(
            HttpExchange exchange)
            throws IOException {

        String username =
                getLoggedUser(exchange);

        if (username == null) {
            redirect(exchange, "/");
            return;
        }

        StringBuilder list =
                new StringBuilder();

        try (Connection con =
                     getConnection()) {

            int userId =
                    getUserId(con, username);

            String sql =
                    """
                    SELECT w.id,
                           p.name,
                           p.price
                    FROM wishlist w
                    JOIN products p
                    ON w.product_id=p.id
                    WHERE w.user_id=?
                    """;

            try (PreparedStatement ps =
                         con.prepareStatement(sql)) {

                ps.setInt(1, userId);

                ResultSet rs =
                        ps.executeQuery();

                while (rs.next()) {

                    int id =
                            rs.getInt("id");

                    String name =
                            rs.getString("name");

                    double price =
                            rs.getDouble("price");

                    list.append("""
                            <div style="
                                background:white;
                                padding:20px;
                                margin:15px;
                                border-radius:10px">

                                <h3>NAME</h3>

                                <p>
                                Rs.PRICE
                                </p>

                                <form action="/remove-wishlist"
                                      method="post">

                                    <input type="hidden"
                                           name="id"
                                           value="ID">

                                    <button>
                                        Remove
                                    </button>

                                </form>

                            </div>
                            """
                            .replace("NAME",
                                    htmlEscape(name))
                            .replace("PRICE",
                                    String.format(
                                            "%.2f",
                                            price))
                            .replace("ID",
                                    String.valueOf(id)));
                }
            }

        } catch (SQLException e) {

            list.append(
                    "<p>" +
                    e.getMessage() +
                    "</p>");
        }

        String html = """
                <html>

                <body style="
                    font-family:Arial;
                    background:#f4f4f4;
                    padding:30px">

                <h1>❤️ My Wishlist</h1>

                LIST

                <br>

                <a href="/dashboard">
                    Continue Shopping
                </a>

                </body>

                </html>
                """
                .replace("LIST",
                        list.toString());

        sendResponse(exchange, html);
    }

    static void removeWishlist(
            HttpExchange exchange)
            throws IOException {

        Map<String, String> form =
                parseForm(readRequest(exchange));

        int id =
                Integer.parseInt(
                        form.get("id"));

        String username =
                getLoggedUser(exchange);

        if (username == null) {
            redirect(exchange, "/");
            return;
        }

        try (Connection con =
                     getConnection()) {

            int userId =
                    getUserId(con, username);

            String sql =
                    "DELETE FROM wishlist " +
                    "WHERE id=? AND user_id=?";

            try (PreparedStatement ps =
                         con.prepareStatement(sql)) {

                ps.setInt(1, id);
                ps.setInt(2, userId);

                ps.executeUpdate();
            }

            redirect(
                    exchange,
                    "/wishlist");

        } catch (SQLException e) {

            sendResponse(
                    exchange,
                    "<h2>" +
                    e.getMessage() +
                    "</h2>");
        }
    }

    // ================= CHECKOUT =================

    static void checkout(
            HttpExchange exchange)
            throws IOException {

        String username =
                getLoggedUser(exchange);

        if (username == null) {
            redirect(exchange, "/");
            return;
        }

        try (Connection con =
                     getConnection()) {

            int userId =
                    getUserId(con, username);

            double total = 0;

            String sql =
                    """
                    SELECT c.quantity,
                           p.price
                    FROM cart c
                    JOIN products p
                    ON c.product_id=p.id
                    WHERE c.user_id=?
                    """;

            try (PreparedStatement ps =
                         con.prepareStatement(sql)) {

                ps.setInt(1, userId);

                ResultSet rs =
                        ps.executeQuery();

                while (rs.next()) {

                    total +=
                            rs.getInt("quantity")
                            * rs.getDouble("price");
                }
            }

            if (total <= 0) {

                sendResponse(
                        exchange,
                        "<h2>Your cart is empty!</h2>" +
                        "<a href='/dashboard'>" +
                        "Continue Shopping</a>");

                return;
            }

            String orderSql =
                    """
                    INSERT INTO orders
                    (user_id,total_amount,status)
                    VALUES (?,?,?)
                    """;

            try (PreparedStatement ps =
                         con.prepareStatement(orderSql)) {

                ps.setInt(1, userId);
                ps.setDouble(2, total);
                ps.setString(3, "Placed");

                ps.executeUpdate();
            }

            String clear =
                    "DELETE FROM cart WHERE user_id=?";

            try (PreparedStatement ps =
                         con.prepareStatement(clear)) {

                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            sendResponse(
                    exchange,
                    """
                    <html>
                    <body style="
                        font-family:Arial;
                        text-align:center;
                        margin-top:100px">

                    <h1>🎉 Order Placed!</h1>

                    <h2>
                    Total: Rs.TOTAL
                    </h2>

                    <br>

                    <a href="/dashboard">
                        Continue Shopping
                    </a>

                    </body>
                    </html>
                    """
                    .replace(
                            "TOTAL",
                            String.format(
                                    "%.2f",
                                    total)));

        } catch (SQLException e) {

            sendResponse(
                    exchange,
                    "<h2>Order Error</h2>" +
                    e.getMessage());
        }
    }

    // ================= ADMIN =================

    static void adminDashboard(
            HttpExchange exchange)
            throws IOException {

        StringBuilder products =
                new StringBuilder();

        try (Connection con =
                     getConnection()) {

            Statement st =
                    con.createStatement();

            ResultSet rs =
                    st.executeQuery(
                            "SELECT * FROM products");

            while (rs.next()) {

                int id =
                        rs.getInt("id");

                String name =
                        rs.getString("name");

                double price =
                        rs.getDouble("price");

                int stock =
                        rs.getInt("stock");

                products.append("""
                        <div style="
                            background:white;
                            padding:20px;
                            margin:15px;
                            border-radius:10px">

                        <h3>NAME</h3>

                        <p>
                            Price: Rs.PRICE
                        </p>

                        <p>
                            Stock: STOCK
                        </p>

                        <form action="/remove-product"
                              method="post">

                            <input type="hidden"
                                   name="id"
                                   value="ID">

                            <button>
                                Remove Product
                            </button>

                        </form>

                        </div>
                        """
                        .replace("NAME",
                                htmlEscape(name))
                        .replace("PRICE",
                                String.format(
                                        "%.2f",
                                        price))
                        .replace("STOCK",
                                String.valueOf(stock))
                        .replace("ID",
                                String.valueOf(id)));
            }

        } catch (SQLException e) {

            products.append(
                    "<p>" +
                    e.getMessage() +
                    "</p>");
        }

        String html = """
                <html>

                <head>

                <title>Admin Dashboard</title>

                </head>

                <body style="
                    font-family:Arial;
                    background:#f4f4f4;
                    padding:30px">

                <h1>LIDIA MART - ADMIN</h1>

                <p>
                    <a href="/new-product">
                        ➕ New Product
                    </a>
                </p>

                <h2>Product Management</h2>

                PRODUCTS

                <br>

                <a href="/logout">
                    Logout
                </a>

                </body>

                </html>
                """
                .replace(
                        "PRODUCTS",
                        products.toString());

        sendResponse(exchange, html);
    }

    // ================= NEW PRODUCT =================

    static void newProduct(
            HttpExchange exchange)
            throws IOException {

        String username =
                getLoggedUser(exchange);

        if (username == null) {
            redirect(exchange, "/");
            return;
        }

        if (exchange.getRequestMethod()
                .equalsIgnoreCase("GET")) {

            String html = """
                    <html>

                    <body style="
                        font-family:Arial;
                        background:#f4f4f4;
                        padding:40px">

                    <h1>Add New Product</h1>

                    <form action="/new-product"
                          method="post">

                        <input name="name"
                               placeholder="Product Name"
                               required><br><br>

                        <input name="category"
                               placeholder="Category"
                               required><br><br>

                        <input name="price"
                               type="number"
                               step="0.01"
                               placeholder="Price"
                               required><br><br>

                        <input name="stock"
                               type="number"
                               placeholder="Stock"
                               required><br><br>

                        <input name="image"
                               placeholder="Image file name">

                        <br><br>

                        <button type="submit">
                            ADD PRODUCT
                        </button>

                    </form>

                    <br>

                    <a href="/dashboard">
                        Back
                    </a>

                    </body>

                    </html>
                    """;

            sendResponse(exchange, html);
            return;
        }

        Map<String, String> form =
                parseForm(readRequest(exchange));

        try (Connection con =
                     getConnection()) {

            String sql =
                    """
                    INSERT INTO products
                    (name,category,price,stock,image)
                    VALUES (?,?,?,?,?)
                    """;

            try (PreparedStatement ps =
                         con.prepareStatement(sql)) {

                ps.setString(
                        1,
                        form.get("name"));

                ps.setString(
                        2,
                        form.get("category"));

                ps.setDouble(
                        3,
                        Double.parseDouble(
                                form.get("price")));

                ps.setInt(
                        4,
                        Integer.parseInt(
                                form.get("stock")));

                ps.setString(
                        5,
                        form.get("image"));

                ps.executeUpdate();
            }

            redirect(
                    exchange,
                    "/dashboard");

        } catch (SQLException e) {

            sendResponse(
                    exchange,
                    "<h2>Add Product Error</h2>" +
                    e.getMessage());
        }
    }

    // ================= REMOVE PRODUCT =================

    static void removeProduct(
            HttpExchange exchange)
            throws IOException {

        Map<String, String> form =
                parseForm(readRequest(exchange));

        int id =
                Integer.parseInt(
                        form.get("id"));

        try (Connection con =
                     getConnection()) {

            try (PreparedStatement ps =
                         con.prepareStatement(
                                 "DELETE FROM products " +
                                 "WHERE id=?")) {

                ps.setInt(1, id);

                ps.executeUpdate();
            }

            redirect(
                    exchange,
                    "/dashboard");

        } catch (SQLException e) {

            sendResponse(
                    exchange,
                    "<h2>Remove Product Error</h2>" +
                    e.getMessage());
        }
    }

    // ================= IMAGE SERVER =================

    static void images(
            HttpExchange exchange)
            throws IOException {

        String path =
                exchange.getRequestURI()
                        .getPath();

        String fileName =
                path.substring("/images/".length());

        Path file =
                Paths.get(
                        IMAGE_FOLDER,
                        fileName);

        if (!Files.exists(file)) {

            exchange.sendResponseHeaders(
                    404,
                    -1);

            exchange.close();
            return;
        }

        String contentType =
                getContentType(fileName);

        exchange.getResponseHeaders()
                .set(
                        "Content-Type",
                        contentType);

        byte[] data =
                Files.readAllBytes(file);

        exchange.sendResponseHeaders(
                200,
                data.length);

        OutputStream out =
                exchange.getResponseBody();

        out.write(data);
        out.close();
    }

    static String getContentType(
            String fileName) {

        String lower =
                fileName.toLowerCase();

        if (lower.endsWith(".png"))
            return "image/png";

        if (lower.endsWith(".jpg") ||
            lower.endsWith(".jpeg"))
            return "image/jpeg";

        if (lower.endsWith(".webp"))
            return "image/webp";

        return "application/octet-stream";
    }

    // ================= LOGOUT =================

    static void logout(
            HttpExchange exchange)
            throws IOException {

        String session =
                getSession(exchange);

        if (session != null)
            sessions.remove(session);

        redirect(exchange, "/");
    }

    // ================= USER ID =================

    static int getUserId(
            Connection con,
            String username)
            throws SQLException {

        String sql =
                "SELECT id FROM users " +
                "WHERE username=?";

        try (PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setString(1, username);

            ResultSet rs =
                    ps.executeQuery();

            if (rs.next())
                return rs.getInt("id");
        }

        return -1;
    }

    // ================= SESSION =================

    static String getLoggedUser(
            HttpExchange exchange) {

        String session =
                getSession(exchange);

        if (session == null)
            return null;

        return sessions.get(session);
    }

    static String getSession(
            HttpExchange exchange) {

        String cookie =
                exchange.getRequestHeaders()
                        .getFirst("Cookie");

        if (cookie == null)
            return null;

        for (String part :
                cookie.split(";")) {

            String[] pair =
                    part.trim()
                        .split("=", 2);

            if (pair.length == 2 &&
                pair[0].equals("SESSION")) {

                return pair[1];
            }
        }

        return null;
    }

    // ================= FORM PARSER =================

    static String readRequest(
            HttpExchange exchange)
            throws IOException {

        return new String(
                exchange.getRequestBody()
                        .readAllBytes(),
                StandardCharsets.UTF_8);
    }

    static Map<String, String> parseForm(
            String data) {

        Map<String, String> map =
                new HashMap<>();

        if (data == null ||
            data.isEmpty())
            return map;

        for (String pair :
                data.split("&")) {

            String[] parts =
                    pair.split("=", 2);

            if (parts.length == 2) {

                map.put(
                        URLDecoder.decode(
                                parts[0],
                                StandardCharsets.UTF_8),

                        URLDecoder.decode(
                                parts[1],
                                StandardCharsets.UTF_8));
            }
        }

        return map;
    }

    // ================= RESPONSE =================

    static void sendResponse(
            HttpExchange exchange,
            String response)
            throws IOException {

        byte[] bytes =
                response.getBytes(
                        StandardCharsets.UTF_8);

        exchange.getResponseHeaders()
                .set(
                        "Content-Type",
                        "text/html; charset=UTF-8");

        exchange.sendResponseHeaders(
                200,
                bytes.length);

        OutputStream output =
                exchange.getResponseBody();

        output.write(bytes);
        output.close();
    }

    static void redirect(
            HttpExchange exchange,
            String location)
            throws IOException {

        exchange.getResponseHeaders()
                .add(
                        "Location",
                        location);

        exchange.sendResponseHeaders(
                302,
                -1);

        exchange.close();
    }

    // ================= HTML ESCAPE =================

    static String htmlEscape(
            String text) {

        if (text == null)
            return "";

        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}