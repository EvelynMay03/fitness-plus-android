<?php

declare(strict_types=1);

require_once __DIR__ . '/config.php';

requirePost();

$data = readJsonInput();

$userId = (int) ($data['user_id'] ?? 0);
$email = normalizeEmail((string) ($data['email'] ?? ''));
$activityType = trim((string) ($data['activity_type'] ?? ''));
$durationMinutes = (int) ($data['duration_minutes'] ?? 0);
$weightKg = isset($data['weight_kg']) && $data['weight_kg'] !== ''
    ? (float) $data['weight_kg']
    : 0.0;
$calories = isset($data['calories']) && $data['calories'] !== ''
    ? (int) $data['calories']
    : null;

if ($activityType === '' || $durationMinutes <= 0 || $weightKg <= 0) {
    jsonResponse(false, 'activity_type, duration_minutes and weight_kg are required', [], 400);
}

try {
    $pdo = db();
    $userId = resolveUserId($pdo, $userId, $email);

    if ($userId <= 0) {
        jsonResponse(false, 'User not found', [], 404);
    }

    $stmt = $pdo->prepare(
        'INSERT INTO workouts (user_id, activity_type, duration_minutes, weight_kg, calories)
         VALUES (:user_id, :activity_type, :duration_minutes, :weight_kg, :calories)'
    );
    $stmt->execute([
        'user_id' => $userId,
        'activity_type' => $activityType,
        'duration_minutes' => $durationMinutes,
        'weight_kg' => $weightKg,
        'calories' => $calories,
    ]);

    jsonResponse(true, 'Workout saved', [
        'workout_id' => (int) $pdo->lastInsertId(),
        'user_id' => $userId,
    ]);
} catch (PDOException $e) {
    jsonResponse(false, 'Failed to save workout', [], 500);
}
