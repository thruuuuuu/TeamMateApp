# Database Configuration
# Place this file in the root of your project

# Database Connection Settings
db.url=jdbc:mysql://localhost:3306/teammate_db
db.username=root
db.password=

# Connection Pool Settings (for future use)
db.pool.initialSize=5
db.pool.maxActive=20
db.pool.maxIdle=10
db.pool.minIdle=5

# Application Settings
app.default.teamsize=5
app.password.minlength=6
app.session.timeout=30

# Logging
log.level=INFO
log.file=logs/teammate_system.log