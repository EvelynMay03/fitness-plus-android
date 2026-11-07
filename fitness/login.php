<?php

declare(strict_types=1);

require_once __DIR__ . '/config.php';

requirePost();

$data = readJsonInput();

$email = normalizeEmail((string) ($data['email'] ?? ''));
$password = (string) ($data['password'] ?? '');

if ($email === '' || $password === '') {
    jsonResponse(false, 'Please enter email and password', [], 400);
}

try {
    $pdo = db();

    $table = DB_TABLE_USER;
    $stmt = $pdo->prepare(
        "SELECT userid, name, email, password FROM {$table} WHERE email = :email LIMIT 1"
    );
    $stmt->execute(['email' => $email]);
    $user = $stmt->fetch();

    if (!$user || !verifyPassword($password, (string) $user['password'])) {
        jsonResponse(false, 'Invalid email or password', [], 401);
    }

    jsonResponse(true, 'Login successful', [
        'user_id' => (int) $user['userid'],
        'name' => $user['name'],
        'email' => $user['email'],
    ]);
} catch (PDOException $e) {
    jsonResponse(false, 'Login failed. Please try again.', [], 500);
}
