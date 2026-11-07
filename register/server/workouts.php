<?php
/**
 * Sample PHP endpoint for FITNESS+ workout logging.
 * Place in your XAMPP htdocs/fitness/ folder alongside login.php and register.php.
 *
 * Required MySQL table:
 *
 * CREATE TABLE workouts (
 *   id INT AUTO_INCREMENT PRIMARY KEY,
 *   user_email VARCHAR(255) NOT NULL,
 *   activity_type VARCHAR(50) NOT NULL,
 *   duration_minutes INT NOT NULL,
 *   distance_km DECIMAL(6,2) NULL,
 *   calories INT NULL,
 *   sets INT NULL,
 *   reps INT NULL,
 *   weight_kg DECIMAL(6,2) NULL,
 *   notes TEXT NULL,
 *   logged_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
 * );
 */

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');

require_once 'db_connect.php'; // your existing DB connection file

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(['success' => false, 'message' => 'POST required']);
    exit;
}

$email = trim($_POST['email'] ?? '');
$activityType = trim($_POST['activity_type'] ?? '');
$duration = (int)($_POST['duration_minutes'] ?? 0);

if ($email === '' || $activityType === '' || $duration <= 0) {
    echo json_encode(['success' => false, 'message' => 'Missing required fields']);
    exit;
}

$distance = isset($_POST['distance_km']) && $_POST['distance_km'] !== '' ? (float)$_POST['distance_km'] : null;
$calories = isset($_POST['calories']) && $_POST['calories'] !== '' ? (int)$_POST['calories'] : null;
$sets = isset($_POST['sets']) && $_POST['sets'] !== '' ? (int)$_POST['sets'] : null;
$reps = isset($_POST['reps']) && $_POST['reps'] !== '' ? (int)$_POST['reps'] : null;
$weight = isset($_POST['weight_kg']) && $_POST['weight_kg'] !== '' ? (float)$_POST['weight_kg'] : null;
$notes = trim($_POST['notes'] ?? '');

$stmt = $conn->prepare(
    'INSERT INTO workouts (user_email, activity_type, duration_minutes, distance_km, calories, sets, reps, weight_kg, notes)
     VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)'
);

$stmt->bind_param(
    'ssiddiiids',
    $email,
    $activityType,
    $duration,
    $distance,
    $calories,
    $sets,
    $reps,
    $weight,
    $notes
);

if ($stmt->execute()) {
    echo json_encode(['success' => true, 'message' => 'Workout saved']);
} else {
    echo json_encode(['success' => false, 'message' => 'Database error']);
}

$stmt->close();
$conn->close();
