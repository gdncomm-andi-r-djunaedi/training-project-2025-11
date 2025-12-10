# Colima Troubleshooting - Common Fixes

## Issue 1: Docker Credential Helper Error

**Error:**
```
error getting credentials - err: exec: "docker-credential-desktop": executable file not found in $PATH
```

**Cause:** Docker config is trying to use Docker Desktop's credential helper.

**Fix:**
```bash
# Remove the credsStore reference
cat > ~/.docker/config.json << 'EOF'
{
  "auths": {
    "https://index.docker.io/v1/": {}
  },
  "currentContext": "colima"
}
EOF

# Then retry docker-compose
docker-compose up -d
```

**Automated Fix Script:**
```bash
# Run this to fix credential issues
mkdir -p ~/.docker
cat > ~/.docker/config.json << 'EOF'
{
  "auths": {},
  "currentContext": "colima"
}
EOF
```

## Issue 2: Docker Compose Version Warning

**Warning:**
```
the attribute `version` is obsolete, it will be ignored
```

**Fix:** Already fixed in the project's docker-compose.yml - the version field has been removed.

## Issue 3: Cannot Connect to Docker Daemon

**Error:**
```
Cannot connect to the Docker daemon
```

**Fix:**
```bash
# Check Colima status
colima status

# If not running, start it
colima start --cpu 4 --memory 8

# Verify
docker ps
```

## Issue 4: Port Already in Use

**Error:**
```
Bind for 0.0.0.0:5432 failed: port is already allocated
```

**Fix:**
```bash
# Find what's using the port
lsof -ti:5432

# Kill the process
lsof -ti:5432 | xargs kill -9

# Or change the port in docker-compose.yml
```

## Issue 5: Containers Not Healthy

**Check Status:**
```bash
docker-compose ps
docker-compose logs postgres
docker-compose logs mongodb
docker-compose logs redis
```

**Fix:**
```bash
# Restart all containers
docker-compose restart

# Or restart specific container
docker-compose restart postgres
```

## Issue 6: Colima VM Out of Resources

**Symptoms:** Slow performance, containers crashing

**Fix:**
```bash
# Stop Colima
colima stop

# Restart with more resources
colima start --cpu 6 --memory 12 --disk 100
```

## Quick Fixes Reference

```bash
# Reset Docker config for Colima
cat > ~/.docker/config.json << 'EOF'
{
  "auths": {},
  "currentContext": "colima"
}
EOF

# Restart everything
colima stop
colima start --cpu 4 --memory 8
docker-compose up -d

# Nuclear option (removes all data)
docker-compose down -v
colima delete
colima start --cpu 4 --memory 8
docker-compose up -d
```

## Prevention Tips

1. **Don't mix Docker Desktop and Colima** - Uninstall Docker Desktop or don't run them simultaneously
2. **Always check Colima is running** before docker commands: `colima status`
3. **Use proper resource allocation** - At least 4 CPU and 8GB RAM for this project
4. **Keep Colima updated**: `brew upgrade colima`

## Still Having Issues?

1. Check Colima logs: `colima logs`
2. SSH into Colima VM: `colima ssh`
3. View Docker logs: `docker-compose logs -f`
4. Check system resources: `docker stats`

For more help, see [COLIMA_SETUP.md](COLIMA_SETUP.md)
