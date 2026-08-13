import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.awt.Desktop;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;

public class LidiaMartWeb {

    static Map<String, User> users = new HashMap<>();
    static Map<String, String> sessions = new HashMap<>();

    static class User {
         String name;
        String username;
        String password;
        String role;

        User(String name, String username, String password, String role) {
            this.name = name;
            this.username = username;
            this.password = password;
            this.role = role;
        }
    }

    public static void main(String[] args) throws Exception {

        // Default Admin
        users.put("admin",
                new User("Administrator", "admin", "admin123", "Admin"));

        HttpServer server = HttpServer.create(
                new InetSocketAddress(8080), 0);

        server.createContext("/", LidiaMartWeb::homePage);
        server.createContext("/login", LidiaMartWeb::login);
        server.createContext("/register", LidiaMartWeb::register);
        server.createContext("/dashboard", LidiaMartWeb::dashboard);
        server.createContext("/logout", LidiaMartWeb::logout);

        server.setExecutor(Executors.newCachedThreadPool());
        server.start();

        System.out.println("====================================");
        System.out.println("       LIDIA MART WEB SERVER");
        System.out.println("====================================");
        System.out.println("Server started!");
        System.out.println("Open: http://localhost:8080");

        // Automatically open browser
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(
                    new URI("http://localhost:8080"));
        }
    }

    // ================= HOME / LOGIN PAGE =================

    static void homePage(HttpExchange exchange) throws IOException {

        String html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>LidiaMart - Login</title>
                    <style>
                        body {
                            margin: 0;
                            font-family: Arial;
                            background: #f4f4f4;
                        }

                        .header {
                            background: #222;
                            color: white;
                            padding: 20px;
                            text-align: center;
                        }

                        .box {
                            width: 350px;
                            margin: 60px auto;
                            background: white;
                            padding: 30px;
                            border-radius: 12px;
                            box-shadow: 0 0 15px #aaa;
                        }

                        h2 {
                            text-align: center;
                        }

                        input {
                            width: 100%;
                            padding: 12px;
                            margin: 10px 0;
                            box-sizing: border-box;
                        }

                        button {
                            width: 100%;
                            padding: 12px;
                            background: #222;
                            color: white;
                            border: none;
                            cursor: pointer;
                        }

                        button:hover {
                            background: #444;
                        }

                        a {
                            display: block;
                            text-align: center;
                            margin-top: 15px;
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

                            <input type="text"
                                   name="username"
                                   placeholder="Enter Username"
                                   required>

                            <input type="password"
                                   name="password"
                                   placeholder="Enter Password"
                                   required>

                            <button type="submit">
                                LOGIN
                            </button>

                        </form>

                        <a href="/register">
                            New User? Register Here
                        </a>

                    </div>

                </body>
                </html>
                """;

        sendResponse(exchange, html);
    }

    // ================= LOGIN =================

    static void login(HttpExchange exchange) throws IOException {

        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            sendResponse(exchange, "Invalid Request");
            return;
        }

        String data = readRequest(exchange);
        Map<String, String> form = parseForm(data);

        String username = form.get("username");
        String password = form.get("password");

        User user = users.get(username);

        if (user != null && user.password.equals(password)) {

            String sessionId = UUID.randomUUID().toString();

            sessions.put(sessionId, username);

            exchange.getResponseHeaders().add(
                    "Set-Cookie",
                    "SESSION=" + sessionId + "; Path=/"
            );

            redirect(exchange, "/dashboard");

        } else {

            String html = """
                    <html>
                    <head>
                    <title>Login Failed</title>
                    <style>
                    body {
                        font-family: Arial;
                        text-align: center;
                        margin-top: 100px;
                    }
                    .error {
                        color: red;
                        font-size: 22px;
                    }
                    a {
                        text-decoration: none;
                    }
                    </style>
                    </head>

                    <body>

                    <h1>LIDIA MART</h1>

                    <p class="error">
                    Invalid Username or Password!
                    </p>

                    <a href="/">
                    Back to Login
                    </a>

                    </body>
                    </html>
                    """;

            sendResponse(exchange, html);
        }
    }

    // ================= REGISTER PAGE =================

    static void register(HttpExchange exchange) throws IOException {

        if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {

            String html = """
                    <!DOCTYPE html>
                    <html>
                    <head>

                    <title>LidiaMart - Register</title>

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
                        box-shadow: 0 0 15px #aaa;
                    }

                    input, select {
                        width: 100%;
                        padding: 12px;
                        margin: 10px 0;
                        box-sizing: border-box;
                    }

                    button {
                        width: 100%;
                        padding: 12px;
                        background: #222;
                        color: white;
                        border: none;
                    }

                    h2 {
                        text-align: center;
                    }

                    </style>

                    </head>

                    <body>

                    <div class="box">

                    <h2>Create Account</h2>

                    <form action="/register" method="post">

                    <input type="text"
                           name="name"
                           placeholder="Enter Full Name"
                           required>

                    <input type="text"
                           name="username"
                           placeholder="Enter Username"
                           required>

                    <input type="password"
                           name="password"
                           placeholder="Enter Password"
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

                    <p>
                    <a href="/">
                    Already have an account? Login
                    </a>
                    </p>

                    </div>

                    </body>
                    </html>
                    """;

            sendResponse(exchange, html);

        } else {

            String data = readRequest(exchange);
            Map<String, String> form = parseForm(data);

            String name = form.get("name");
            String username = form.get("username");
            String password = form.get("password");
            String role = form.get("role");

            if (users.containsKey(username)) {

                sendResponse(exchange,
                        "<h2>Username already exists!</h2>" +
                        "<a href='/register'>Try Again</a>");

                return;
            }

            users.put(username,
                    new User(name, username, password, role));

            String html = """
                    <html>

                    <head>
                    <title>Registration Successful</title>
                    </head>

                    <body style="text-align:center;
                                 font-family:Arial;
                                 margin-top:100px;">

                    <h1>Registration Successful!</h1>

                    <p>Your account has been created.</p>

                    <a href="/">
                    Go to Login
                    </a>

                    </body>

                    </html>
                    """;

            sendResponse(exchange, html);
        }
    }

    // ================= DASHBOARD =================

    static void dashboard(HttpExchange exchange) throws IOException {

        String sessionId = getSession(exchange);

        if (sessionId == null ||
                !sessions.containsKey(sessionId)) {

            redirect(exchange, "/");
            return;
        }

        String username = sessions.get(sessionId);
        User user = users.get(username);

        if (user.role.equals("Buyer")) {

            buyerDashboard(exchange, user);

        } else if (user.role.equals("Seller")) {

            sellerDashboard(exchange, user);

        } else {

            adminDashboard(exchange, user);
        }
    }

    // ================= BUYER DASHBOARD =================

    static void buyerDashboard(
            HttpExchange exchange, User user)
            throws IOException {

        String html = """
                <html>
                <head>

                <title>Buyer Home</title>

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
                }

                .container {
                    padding: 30px;
                }

                .card {
                    background: white;
                    padding: 20px;
                    margin: 15px 0;
                    border-radius: 10px;
                }

                a {
                    text-decoration: none;
                    color: red;
                }

                </style>

                </head>

                <body>

                <div class="header">

                <h1>LIDIA MART</h1>

                <p>Welcome Buyer: USERNAME</p>

                </div>

                <div class="container">

                <div class="card">
                <h2>Buyer Home</h2>
                <p>Browse and purchase accessories.</p>
                </div>

                <div class="card">
                <h3>Products</h3>
                <p>Necklace - Rs.500</p>
                <p>Bracelet - Rs.300</p>
                <p>Earrings - Rs.250</p>
                <p>Handbag - Rs.800</p>
                </div>

                <div class="card">
                <h3>Shopping Cart</h3>
                <p>Your cart is currently empty.</p>
                </div>

                <a href="/logout">
                Logout
                </a>

                </div>

                </body>
                </html>
                """;

        html = html.replace("USERNAME", user.name);

        sendResponse(exchange, html);
    }

    // ================= SELLER DASHBOARD =================

    static void sellerDashboard(
            HttpExchange exchange, User user)
            throws IOException {

        String html = """
                <html>
                <head>

                <title>Seller Dashboard</title>

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
                }

                .container {
                    padding: 30px;
                }

                .card {
                    background: white;
                    padding: 25px;
                    margin: 15px 0;
                    border-radius: 10px;
                }

                a {
                    color: red;
                }

                </style>

                </head>

                <body>

                <div class="header">

                <h1>LIDIA MART</h1>

                <p>Seller Dashboard</p>

                </div>

                <div class="container">

                <div class="card">

                <h2>Welcome, USERNAME</h2>

                <p>Add and manage your products.</p>

                </div>

                <div class="card">

                <h3>Seller Options</h3>

                <p>➕ Add Product</p>
                <p>✏ Edit Product</p>
                <p>🗑 Delete Product</p>
                <p>📦 View Orders</p>

                </div>

                <a href="/logout">
                Logout
                </a>

                </div>

                </body>
                </html>
                """;

        html = html.replace("USERNAME", user.name);

        sendResponse(exchange, html);
    }

    // ================= ADMIN DASHBOARD =================

    static void adminDashboard(
            HttpExchange exchange, User user)
            throws IOException {

        StringBuilder userList = new StringBuilder();

        for (User u : users.values()) {

            userList.append("<p>")
                    .append(u.name)
                    .append(" - ")
                    .append(u.username)
                    .append(" - ")
                    .append(u.role)
                    .append("</p>");
        }

        String html = """
                <html>

                <head>

                <title>Admin Dashboard</title>

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
                }

                .container {
                    padding: 30px;
                }

                .card {
                    background: white;
                    padding: 25px;
                    margin: 15px 0;
                    border-radius: 10px;
                }

                a {
                    color: red;
                }

                </style>

                </head>

                <body>

                <div class="header">

                <h1>LIDIA MART</h1>

                <p>ADMIN DASHBOARD</p>

                </div>

                <div class="container">

                <div class="card">

                <h2>Registered Users</h2>

                USERLIST

                </div>

                <div class="card">

                <h2>Orders</h2>

                <p>No orders available.</p>

                </div>

                <div class="card">

                <h2>Product Management</h2>

                <p>View Products</p>
                <p>Remove Products</p>

                </div>

                <a href="/logout">
                Logout
                </a>

                </div>

                </body>

                </html>
                """;

        html = html.replace("USERLIST", userList.toString());

        sendResponse(exchange, html);
    }

    // ================= LOGOUT =================

    static void logout(HttpExchange exchange)
            throws IOException {

        String sessionId = getSession(exchange);

        if (sessionId != null) {
            sessions.remove(sessionId);
        }

        redirect(exchange, "/");
    }

    // ================= HELPER METHODS =================

    static String readRequest(HttpExchange exchange)
            throws IOException {

        InputStream input =
                exchange.getRequestBody();

        return new String(
                input.readAllBytes(),
                StandardCharsets.UTF_8);
    }

    static Map<String, String> parseForm(String data) {

        Map<String, String> map = new HashMap<>();

        for (String pair : data.split("&")) {

            String[] parts = pair.split("=", 2);

            if (parts.length == 2) {

                map.put(
                        URLDecoder.decode(
                                parts[0],
                                StandardCharsets.UTF_8),

                        URLDecoder.decode(
                                parts[1],
                                StandardCharsets.UTF_8)
                );
            }
        }

        return map;
    }

    static String getSession(HttpExchange exchange) {

        String cookie =
                exchange.getRequestHeaders()
                        .getFirst("Cookie");

        if (cookie == null)
            return null;

        for (String part : cookie.split(";")) {

            String[] pair =
                    part.trim().split("=", 2);

            if (pair.length == 2 &&
                    pair[0].equals("SESSION")) {

                return pair[1];
            }
        }

        return null;
    }

    static void redirect(
            HttpExchange exchange,
            String location)
            throws IOException {

        exchange.getResponseHeaders()
                .add("Location", location);

        exchange.sendResponseHeaders(302, -1);
        exchange.close();
    }

    static void sendResponse(
            HttpExchange exchange,
            String response)
            throws IOException {

        byte[] bytes =
                response.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders()
                .set("Content-Type",
                        "text/html; charset=UTF-8");

        exchange.sendResponseHeaders(
                200, bytes.length);

        OutputStream output =
                exchange.getResponseBody();

        output.write(bytes);
        output.close();
    }
}