<?php

declare(strict_types=1);

require_once __DIR__ . '/config.php';

requireGet();

try {
    $pdo = db();

    $stmt = $pdo->query(
        'SELECT activity_key, name, description, emoji
         FROM activities
         ORDER BY id ASC'
    );

    $activities = $stmt->fetchAll();

    http_response_code(200);
    echo json_encode($activities);
} catch (PDOException $e) {
    jsonResponse(false, 'Failed to load activities', [], 500);
}
