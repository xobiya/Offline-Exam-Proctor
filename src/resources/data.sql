-- Admin User
INSERT INTO users (username, password_hash, role, full_name, email)
VALUES (
    'admin01',
    SHA2('admin123', 256),
    'admin',
    'Feleke Eshetu',
    'admin01@example.com'
);

-- Student Users (password is the same as the username, all lowercase, no spaces)
INSERT INTO users (username, password_hash, role, full_name, email)
VALUES
('abiyotsimeneh', SHA2('abiyotsimeneh', 256), 'student', 'Abiyot Simeneh', 'abiyotsimeneh@example.com'),
('eyobabukiya', SHA2('eyobabukiya', 256), 'student', 'Eyob Abukiya', 'eyobabukiya@example.com'),
('yonatanshitaye', SHA2('yonatanshitaye', 256), 'student', 'Yonatan Shitaye Gatiso', 'yonatanshitaye@example.com'),
('zinashgetiso', SHA2('zinashgetiso', 256), 'student', 'Zinash Getiso Sendno', 'zinashgetiso@example.com'),
('sinishawyohannes', SHA2('sinishawyohannes', 256), 'student', 'Sinishaw Yohannes', 'sinishawyohannes@example.com'),
('yenenehamots', SHA2('yenenehamots', 256), 'student', 'Yeneneh Amots', 'yenenehamots@example.com'),
('neimamohammed', SHA2('neimamohammed', 256), 'student', 'Neima Mohammed Hussen', 'neimamohammed@example.com'),
('aysanshimels', SHA2('aysanshimels', 256), 'student', 'Aysan Shimels Tasaw', 'aysanshimels@example.com'),
('dagimworku', SHA2('dagimworku', 256), 'student', 'Dagim Worku Girma', 'dagimworku@example.com'),
('mariamawitnejib', SHA2('mariamawitnejib', 256), 'student', 'Mariamawit Nejib', 'mariamawitnejib@example.com');

-- Passwords for each user are the same as their username (all lowercase, no spaces). For example:
-- Username: abiyotsimeneh, Password: abiyotsimeneh
-- Username: dagimworku, Password: dagimworku
-- Admin username: admin01, Password: admin123

-- Demo Exam: Java
INSERT INTO exams (title, description, duration_minutes, entry_password, exit_password)
VALUES ('Java Demo Exam', 'Demo exam for Java programming', 60, SHA2('123', 256), SHA2('321', 256));

-- Demo Questions for Java Demo Exam (exam_id = 1 assumed for demo)
INSERT INTO questions (exam_id, question_text, option_a, option_b, option_c, option_d, correct_option) VALUES
(1, 'What is the size of int in Java?', '2 bytes', '4 bytes', '8 bytes', 'Depends on OS', 'B'),
(1, 'Which keyword is used to inherit a class in Java?', 'this', 'super', 'extends', 'implements', 'C'),
(1, 'Which method is the entry point of a Java program?', 'start()', 'main()', 'run()', 'init()', 'B'),
(1, 'Which of these is not a Java primitive type?', 'int', 'float', 'String', 'char', 'C'),
(1, 'What is the default value of a boolean variable?', 'true', 'false', '0', 'null', 'B'),
(1, 'Which operator is used for comparison?', '=', '==', '!=', 'equals', 'B'),
(1, 'Which of these is a valid array declaration?', 'int arr[];', 'array int[];', 'int[] arr;', 'A and C', 'D'),
(1, 'Which package contains Scanner class?', 'java.util', 'java.io', 'java.lang', 'java.awt', 'A'),
(1, 'What is the output of 3 + 4 + "Java"?', '7Java', 'Java7', '34Java', 'Java34', 'A'),
(1, 'Which is not an access modifier?', 'public', 'private', 'protected', 'package', 'D'),
(1, 'Which is used to handle exceptions?', 'try-catch', 'if-else', 'for', 'switch', 'A'),
(1, 'Which is not a loop in Java?', 'for', 'while', 'repeat', 'do-while', 'C'),
(1, 'Which is the parent class of all classes?', 'Object', 'Class', 'Main', 'Base', 'A'),
(1, 'Which is used to create an object?', 'new', 'class', 'object', 'create', 'A'),
(1, 'Which is not a valid identifier?', 'myVar', '2var', '_var', 'var2', 'B'),
(1, 'Which is used for comments in Java?', '//', '#', '--', '/* */', 'A'),
(1, 'Which is not a Java keyword?', 'static', 'void', 'main', 'int', 'C'),
(1, 'Which is used for inheritance?', 'extends', 'implements', 'inherits', 'derives', 'A'),
(1, 'Which is not a method of String?', 'length()', 'charAt()', 'substring()', 'push()', 'D'),
(1, 'Which is used to stop a loop?', 'break', 'continue', 'exit', 'stop', 'A'),
(1, 'Which is not a valid data type?', 'byte', 'short', 'integer', 'long', 'C'),
(1, 'Which is used to import a package?', 'import', 'package', 'include', 'using', 'A'),
(1, 'Which is not a logical operator?', '&&', '||', '!', '&', 'D'),
(1, 'Which is used to define a constant?', 'final', 'const', 'static', 'define', 'A'),
(1, 'Which is not a wrapper class?', 'Integer', 'Float', 'Character', 'String', 'D'),
(1, 'Which is used to read input?', 'Scanner', 'Input', 'Reader', 'Buffer', 'A'),
(1, 'Which is not a valid loop?', 'for', 'while', 'do-while', 'foreach', 'D'),
(1, 'Which is used to compare strings?', '==', 'equals()', 'compare()', 'compareTo()', 'B'),
(1, 'Which is not a valid statement?', 'int x = 5;', 'float y = 2.5;', 'char c = "a";', 'boolean b = true;', 'C'),
(1, 'Which is used to exit from a method?', 'return', 'break', 'continue', 'exit', 'A');

