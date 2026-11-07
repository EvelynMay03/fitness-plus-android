<?php
/**
 * Save and fetch daily calorie goals for FITNESS+.
 * Place in your XAMPP htdocs/fitness/ folder.
 *
 * Required MySQL table:
 *
 * CREATE TABLE goals (
 *     id BIGINT PRIMARY KEY AUTO_INCREMENT,
 *     user_id INT NOT NULL,
 *     goals INT NOT NULL,
 *     goal_date DATE NOT NULL,
 *     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 *     FOREIGN KEY (user_id) REFERENCES user(userid),
 *     UNIQUE KEY unique_user_goal_date (user_id, goal_date)
 * );
 *
 * POST:
 *   user_id   (optional if email is sent)
 *   email     (optional fallback to find user_id)
 *   goals     (required calorie target, e.g. 500)
 *   goal_date (optional, defaults to today, format YYYY-MM-DD)
 *
 * GET:
 *   user_id   (optional if email is sent)
 *   email     (optional fallback to find user_id)
 *   goal_date (optional, defaults to today)
 */

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');

require_once 'db_connect.php';

function resolveUserId(mysqli $conn, int $userId, string $email): int
{
    if ($userId > 0) {
        return $userId;
    }

    if ($email === '') {
        return 0;
    }

    $lookup = $conn->prepare('SELECT userid FROM user WHERE email = ? LIMIT 1');
    $lookup->bind_param('s', $email);
    $lookup->execute();
    $result = $lookup->get_result();
    $row = $result->fetch_assoc();
    $lookup->close();

    return $row ? (int)$row['userid'] : 0;
}

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $userId = (int)($_GET['user_id'] ?? 0);
    $email = trim(strtolower($_GET['email'] ?? ''));
    $goalDate = trim($_GET['goal_date'] ?? '');

    if ($goalDate === '') {
        $goalDate = date('Y-m-d');
    }

    $userId = resolveUserId($conn, $userId, $email);

    if ($userId <= 0) {
        echo json_encode(['success' => false, 'message' => 'User not found']);
        exit;
    }

    $stmt = $conn->prepare(
        'SELECT goals, goal_date
         FROM goals
         WHERE user_id = ? AND goal_date = ?
         LIMIT 1'
    );
    $stmt->bind_param('is', $userId, $goalDate);
    $stmt->execute();
    $result = $stmt->get_result();
    $goal = $result->fetch_assoc();
    $stmt->close();

    if (!$goal) {
        echo json_encode([
            'success' => true,
            'message' => 'No goal set for this date',
            'user_id' => $userId,
            'goal_date' => $goalDate,
            'goals' => null,
        ]);
        exit;
    }

    echo json_encode([
        'success' => true,
        'message' => 'Goal found',
        'user_id' => $userId,
        'goal_date' => $goal['goal_date'],
        'goals' => (int)$goal['goals'],
    ]);

    $conn->close();
    exit;
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(['success' => false, 'message' => 'POST or GET required']);
    exit;
}

$userId = (int)($_POST['user_id'] ?? 0);
$email = trim(strtolower($_POST['email'] ?? ''));
$goals = (int)($_POST['goals'] ?? 0);
$goalDate = trim($_POST['goal_date'] ?? '');

if ($goalDate === '') {
    $goalDate = date('Y-m-d');
}

if ($goals <= 0) {
    echo json_encode(['success' => false, 'message' => 'Invalid calorie goal']);
    exit;
}

$userId = resolveUserId($conn, $userId, $email);

if ($userId <= 0) {
    echo json_encode(['success' => false, 'message' => 'User not found']);
    exit;
}

$stmt = $conn->prepare(
    'INSERT INTO goals (user_id, goals, goal_date)
     VALUES (?, ?, ?)
     ON DUPLICATE KEY UPDATE goals = VALUES(goals)'
);

$stmt->bind_param('iis', $userId, $goals, $goalDate);

if ($stmt->execute()) {
    echo json_encode([
        'success' => true,
        'message' => 'Goal saved',
        'user_id' => $userId,
        'goals' => $goals,
        'goal_date' => $goalDate,
    ]);
} else {
    echo json_encode(['success' => false, 'message' => 'Database error']);
}

$stmt->close();
$conn->close();
