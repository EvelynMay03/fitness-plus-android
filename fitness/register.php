<?php

declare(strict_types=1);

require_once __DIR__ . '/config.php';

requirePost();

$data = readJsonInput();

$name = trim((string) ($data['name'] ?? ''));
$email = normalizeEmail((string) ($data['email'] ?? ''));
$password = (string) ($data['password'] ?? '');

if ($name === '' || $email === '' || $password === '') {
    jsonResponse(false, 'Please fill in all fields', [], 400);
}

if (!isValidEmail($email)) {
    jsonResponse(false, 'Please enter a valid email', [], 400);
}

if (strlen($password) < 6) {
    jsonResponse(false, 'Password must be at least 6 characters', [], 400);
}

try {
    $pdo = db();

    $table = DB_TABLE_USER;
    $check = $pdo->prepare("SELECT userid FROM {$table} WHERE email = :email LIMIT 1");
    $check->execute(['email' => $email]);

    if ($check->fetch()) {
        jsonResponse(false, 'An account with this email already exists', [], 409);
    }

    $insert = $pdo->prepare(
        "INSERT INTO {$table} (name, email, password) VALUES (:name, :email, :password)"
    );
    $insert->execute([
        'name' => $name,
        'email' => $email,
        'password' => password_hash($password, PASSWORD_DEFAULT),
    ]);

    jsonResponse(true, 'Account created', [
        'user_id' => (int) $pdo->lastInsertId(),
        'name' => $name,
        'email' => $email,
    ], 201);
} catch (PDOException $e) {
    jsonResponse(false, 'Registration failed. Please try again.', [], 500);
}
