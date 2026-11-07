<?php

declare(strict_types=1);

header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(204);
    exit;
}

const DB_HOST = 'localhost';
const DB_NAME = 'Fitness';
const DB_TABLE_USER = 'user';
const DB_USER = 'root';
const DB_PASS = '';

function db(): PDO
{
    static $pdo = null;

    if ($pdo === null) {
        $dsn = 'mysql:host=' . DB_HOST . ';dbname=' . DB_NAME . ';charset=utf8mb4';
        $pdo = new PDO($dsn, DB_USER, DB_PASS, [
            PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
        ]);
    }

    return $pdo;
}

function readJsonInput(): array
{
    $raw = file_get_contents('php://input');
    if ($raw === false || $raw === '') {
        return $_POST;
    }

    $decoded = json_decode($raw, true);
    return is_array($decoded) ? $decoded : $_POST;
}

function readInput(): array
{
    if ($_SERVER['REQUEST_METHOD'] === 'GET') {
        return $_GET;
    }

    return readJsonInput();
}

function jsonResponse(bool $success, string $message, array $extra = [], int $status = 200): void
{
    http_response_code($status);
    echo json_encode(array_merge([
        'success' => $success,
        'message' => $message,
    ], $extra));
    exit;
}

function requirePost(): void
{
    if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
        jsonResponse(false, 'Only POST requests are allowed', [], 405);
    }
}

function requireGet(): void
{
    if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
        jsonResponse(false, 'Only GET requests are allowed', [], 405);
    }
}

function normalizeEmail(string $email): string
{
    return strtolower(trim($email));
}

function isValidEmail(string $email): bool
{
    return filter_var($email, FILTER_VALIDATE_EMAIL) !== false;
}

function resolveUserId(PDO $pdo, int $userId, string $email): int
{
    if ($userId > 0) {
        return $userId;
    }

    if ($email === '') {
        return 0;
    }

    $table = DB_TABLE_USER;
    $stmt = $pdo->prepare("SELECT userid FROM {$table} WHERE email = :email LIMIT 1");
    $stmt->execute(['email' => $email]);

    $user = $stmt->fetch();
    return $user ? (int) $user['userid'] : 0;
}

function verifyPassword(string $plainPassword, string $storedPassword): bool
{
    if ($storedPassword === '') {
        return false;
    }

    if (password_verify($plainPassword, $storedPassword)) {
        return true;
    }

    $sha256Hash = hash('sha256', $plainPassword);
    return hash_equals($storedPassword, $sha256Hash);
}
