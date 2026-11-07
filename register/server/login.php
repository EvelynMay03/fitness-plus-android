<?php
/**
 * Login endpoint for the FITNESS+ Android app.
 * Place in your XAMPP htdocs/fitness/ folder.
 *
 * Expected MySQL table:
 *
 * CREATE TABLE user (
 *     userid INT PRIMARY KEY AUTO_INCREMENT,
 *     name VARCHAR(100) NOT NULL,
 *     email VARCHAR(255) NOT NULL UNIQUE,
 *     password VARCHAR(255) NOT NULL
 * );
 *
 * The Android app sends the password as plain text and hashes it with SHA-256
 * on the server before comparing it to the stored hash.
 */

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');

require_once 'db_connect.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(['success' => false, 'message' => 'POST required']);
    exit;
}

$email = trim(strtolower($_POST['email'] ?? ''));
$password = $_POST['password'] ?? '';

if ($email === '' || $password === '') {
    echo json_encode(['success' => false, 'message' => 'Email and password are required']);
    exit;
}

$stmt = $conn->prepare(
    'SELECT userid, name, email, password
     FROM user
     WHERE email = ?
     LIMIT 1'
);

$stmt->bind_param('s', $email);
$stmt->execute();
$result = $stmt->get_result();
$user = $result->fetch_assoc();
$stmt->close();

if (!$user) {
    echo json_encode(['success' => false, 'message' => 'Invalid email or password']);
    exit;
}

$storedPassword = $user['password'];
$inputHash = hash('sha256', $password);
$passwordValid = hash_equals($storedPassword, $inputHash)
    || password_verify($password, $storedPassword);

if (!$passwordValid) {
    echo json_encode(['success' => false, 'message' => 'Invalid email or password']);
    exit;
}

echo json_encode([
    'success' => true,
    'message' => 'Login successful',
    'name' => $user['name'],
    'email' => $user['email'],
    'user_id' => (int)$user['userid'],
]);

$conn->close();
