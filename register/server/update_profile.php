<?php
/**
 * Sample endpoint to update user profile (name only).
 */

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');

require_once 'db_connect.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(['success' => false, 'message' => 'POST required']);
    exit;
}

$email = trim(strtolower($_POST['email'] ?? ''));
$name = trim($_POST['name'] ?? '');

if ($email === '' || $name === '') {
    echo json_encode(['success' => false, 'message' => 'Email and name are required']);
    exit;
}

$stmt = $conn->prepare('UPDATE users SET name = ? WHERE email = ?');
$stmt->bind_param('ss', $name, $email);

if ($stmt->execute() && $stmt->affected_rows >= 0) {
    echo json_encode(['success' => true, 'message' => 'Profile updated']);
} else {
    echo json_encode(['success' => false, 'message' => 'Update failed']);
}

$stmt->close();
$conn->close();
