<?php
/**
 * Sample endpoint to change user password.
 */

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');

require_once 'db_connect.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(['success' => false, 'message' => 'POST required']);
    exit;
}

$email = trim(strtolower($_POST['email'] ?? ''));
$currentPassword = $_POST['current_password'] ?? '';
$newPassword = $_POST['new_password'] ?? '';

if ($email === '' || $currentPassword === '' || $newPassword === '') {
    echo json_encode(['success' => false, 'message' => 'All fields are required']);
    exit;
}

if (strlen($newPassword) < 6) {
    echo json_encode(['success' => false, 'message' => 'Password must be at least 6 characters']);
    exit;
}

$stmt = $conn->prepare('SELECT password FROM users WHERE email = ?');
$stmt->bind_param('s', $email);
$stmt->execute();
$result = $stmt->get_result();
$user = $result->fetch_assoc();
$stmt->close();

if (!$user) {
    echo json_encode(['success' => false, 'message' => 'User not found']);
    exit;
}

$storedHash = $user['password'];
$currentHash = hash('sha256', $currentPassword);

if ($storedHash !== $currentHash && !password_verify($currentPassword, $storedHash)) {
    echo json_encode(['success' => false, 'message' => 'Current password is incorrect']);
    exit;
}

$newHash = hash('sha256', $newPassword);
$update = $conn->prepare('UPDATE users SET password = ? WHERE email = ?');
$update->bind_param('ss', $newHash, $email);

if ($update->execute()) {
    echo json_encode(['success' => true, 'message' => 'Password updated']);
} else {
    echo json_encode(['success' => false, 'message' => 'Update failed']);
}

$update->close();
$conn->close();
