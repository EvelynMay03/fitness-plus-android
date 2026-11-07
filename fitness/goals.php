<?php

declare(strict_types=1);

require_once __DIR__ . '/config.php';

$method = $_SERVER['REQUEST_METHOD'];

if ($method === 'GET') {
    $data = readInput();

    $userId = (int) ($data['user_id'] ?? 0);
    $email = normalizeEmail((string) ($data['email'] ?? ''));
    $goalDate = trim((string) ($data['goal_date'] ?? ''));

    if ($goalDate === '') {
        $goalDate = date('Y-m-d');
    }

    try {
        $pdo = db();
        $userId = resolveUserId($pdo, $userId, $email);

        if ($userId <= 0) {
            jsonResponse(false, 'User not found', [], 404);
        }

        $stmt = $pdo->prepare(
            'SELECT goals, goal_date
             FROM goals
             WHERE user_id = :user_id AND goal_date = :goal_date
             LIMIT 1'
        );
        $stmt->execute([
            'user_id' => $userId,
            'goal_date' => $goalDate,
        ]);
        $goal = $stmt->fetch();

        if (!$goal) {
            jsonResponse(true, 'No goal set for this date', [
                'user_id' => $userId,
                'goal_date' => $goalDate,
                'goals' => null,
            ]);
        }

        jsonResponse(true, 'Goal found', [
            'user_id' => $userId,
            'goal_date' => $goal['goal_date'],
            'goals' => (int) $goal['goals'],
        ]);
    } catch (PDOException $e) {
        jsonResponse(false, 'Failed to load goal', [], 500);
    }
}

if ($method !== 'POST') {
    jsonResponse(false, 'Only GET or POST requests are allowed', [], 405);
}

$data = readJsonInput();

$userId = (int) ($data['user_id'] ?? 0);
$email = normalizeEmail((string) ($data['email'] ?? ''));
$goals = (int) ($data['goals'] ?? 0);
$goalDate = trim((string) ($data['goal_date'] ?? ''));

if ($goalDate === '') {
    $goalDate = date('Y-m-d');
}

if ($goals <= 0) {
    jsonResponse(false, 'Invalid calorie goal', [], 400);
}

try {
    $pdo = db();
    $userId = resolveUserId($pdo, $userId, $email);

    if ($userId <= 0) {
        jsonResponse(false, 'User not found', [], 404);
    }

    $stmt = $pdo->prepare(
        'INSERT INTO goals (user_id, goals, goal_date)
         VALUES (:user_id, :goals, :goal_date)
         ON DUPLICATE KEY UPDATE goals = VALUES(goals)'
    );
    $stmt->execute([
        'user_id' => $userId,
        'goals' => $goals,
        'goal_date' => $goalDate,
    ]);

    jsonResponse(true, 'Goal saved', [
        'user_id' => $userId,
        'goals' => $goals,
        'goal_date' => $goalDate,
    ]);
} catch (PDOException $e) {
    jsonResponse(false, 'Failed to save goal', [], 500);
}
