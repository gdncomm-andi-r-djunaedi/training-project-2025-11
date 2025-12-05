# training-project-2025-11
requirements : https://gdncomm.atlassian.net/wiki/spaces/GDNIT/pages/1787920458/2025+QA+to+BE+Conversion+Program+-+Final+Project


Run the database first using this command inside the repo folder : podman-compose up -d
It will automatically init the DB tables and copy the DB data

Run redis with command : brew services start redis

Command to stop the podman : podman-compose down -v

Database access : 

Host: localhost
Port: 5432
Database: product_db
Username: appuser
Password: apppassword

Host: localhost
Port: 5432
Database: customer_db
Username: appuser
Password: apppassword

Host: localhost
Port: 5432
Database: cart_db
Username: appuser
Password: apppassword