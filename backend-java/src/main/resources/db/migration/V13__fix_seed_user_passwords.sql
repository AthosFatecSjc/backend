UPDATE energia.app_user
SET password = '$2a$10$x.az1sFzcUqnd6hPVre/POF1qfOOVNmVUPZ/O3h6N0LoFbyV7jdJ6'
WHERE email IN ('active@example.com', 'pending@example.com', 'rejected@example.com');
