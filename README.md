# Booking System

## Redis Deployment

### Prerequisites
- Docker and Docker Compose installed
- At least 1GB of free memory for Redis

### Deployment Steps

1. Start Redis:
```bash
docker-compose up -d redis
```

2. Verify Redis is running:
```bash
docker-compose ps redis
```

3. Check Redis logs:
```bash
docker-compose logs redis
```

4. Connect to Redis CLI:
```bash
docker-compose exec redis redis-cli
```

### Redis Configuration

The Redis instance is configured with the following settings:
- Port: 6379
- Persistence: AOF (Append Only File) enabled
- Max Memory: 512MB
- Memory Policy: allkeys-lru (Least Recently Used)
- Health Check: Every 10 seconds
- Auto-restart: Enabled

### Monitoring

1. Check Redis memory usage:
```bash
docker-compose exec redis redis-cli info memory
```

2. Monitor Redis commands:
```bash
docker-compose exec redis redis-cli monitor
```

3. Check Redis status:
```bash
docker-compose exec redis redis-cli ping
```

### Backup and Restore

1. Create a backup:
```bash
docker-compose exec redis redis-cli SAVE
docker cp booking-redis:/data/dump.rdb ./redis-backup/
```

2. Restore from backup:
```bash
docker cp ./redis-backup/dump.rdb booking-redis:/data/
docker-compose restart redis
```

### Troubleshooting

1. If Redis fails to start:
```bash
docker-compose logs redis
```

2. If memory issues occur:
```bash
docker-compose exec redis redis-cli info memory
```

3. To reset Redis data:
```bash
docker-compose down -v
docker-compose up -d redis
```

### Security Notes

1. The Redis instance is configured for local development. For production:
   - Set a strong password
   - Enable SSL/TLS
   - Restrict network access
   - Use Redis ACLs

2. Update the application.yml with the appropriate security settings before deploying to production. 