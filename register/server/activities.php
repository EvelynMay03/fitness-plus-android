<?php
/**
 * Returns all activity types from the activities table as JSON.
 * Place in your XAMPP htdocs/fitness/ folder.
 */

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');

require_once 'db_connect.php';

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    echo json_encode(['success' => false, 'message' => 'GET required']);
    exit;
}

$result = $conn->query(
    'SELECT activity_key, name, description, emoji
     FROM activities
     ORDER BY id ASC'
);

$activities = [];

if ($result) {
    while ($row = $result->fetch_assoc()) {
        $activities[] = [
            'activity_key' => $row['activity_key'],
            'name' => $row['name'],
            'description' => $row['description'],
            'emoji' => $row['emoji'],
        ];
    }
    $result->free();
}

echo json_encode($activities);

$conn->close();
