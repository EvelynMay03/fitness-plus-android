<?php
/**
 * Database connection for FITNESS+ PHP APIs.
 * Update the values below to match your XAMPP MySQL setup.
 */

$host = getenv('DB_HOST') ?: 'localhost';
$dbName = getenv('DB_NAME') ?: 'fitness';
$dbUser = getenv('DB_USER') ?: '';
$dbPass = getenv('DB_PASSWORD') ?: '';

$conn = new mysqli($host, $dbUser, $dbPass, $dbName);

if ($conn->connect_error) {
    header('Content-Type: application/json');
    echo json_encode([
        'success' => false,
        'message' => 'Database connection failed',
    ]);
    exit;
}

$conn->set_charset('utf8mb4');
