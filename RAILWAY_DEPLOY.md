# 🚀 Guía de Despliegue en Railway

## Requisitos previos
- Cuenta en [Railway.app](https://railway.app)
- Git configurado localmente
- Repositorio Git para el proyecto

## Pasos para desplegar

### 1. **Inicializar proyecto en Railway**
```bash
npm install -g @railway/cli
railway login
railway init
```

### 2. **Seleccionar opciones**
- Elige "Dockerfile" como builder
- Selecciona desplegar desde el directorio actual

### 3. **Desplegar**
```bash
railway up
```

O alternativamente:

### **Opción: Desplegar desde GitHub**
1. Sube el repositorio a GitHub
2. En Railway, crea un nuevo proyecto
3. Conecta con GitHub y selecciona el repositorio
4. Railway detectará automáticamente el Dockerfile
5. Configura variables de entorno (ver sección abajo)
6. Haz deploy

## ⚙️ Variables de Entorno en Railway

Agrega estas variables en Railway Dashboard → Variables:

```env
# Puerto (Railway lo asigna automáticamente en $PORT)
SERVER_PORT=${PORT:8080}

# Base de datos (si usas)
SPRING_DATASOURCE_URL=jdbc:postgresql://host:5432/db
SPRING_DATASOURCE_USERNAME=user
SPRING_DATASOURCE_PASSWORD=password

# Redis (si usas)
SPRING_REDIS_HOST=redis-host
SPRING_REDIS_PORT=6379
SPRING_REDIS_PASSWORD=redis-password

# RabbitMQ (si usas AMQP)
SPRING_RABBITMQ_HOST=rabbitmq-host
SPRING_RABBITMQ_PORT=5672
SPRING_RABBITMQ_USERNAME=guest
SPRING_RABBITMQ_PASSWORD=guest

# Perfil de aplicación
SPRING_PROFILES_ACTIVE=prod

# Logging
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_COM_PRODUCTOS=DEBUG
```

## 🔗 Conectar servicios en Railway

Railway permite conectar servicios automáticamente. En el dashboard:

1. Agrega dependencias (PostgreSQL, Redis, RabbitMQ)
2. Los valores de conexión se inyectarán automáticamente como variables de entorno

## 📝 Puerto dinámico

Railway asigna un puerto dinámico en la variable `$PORT`. Asegúrate que Spring Boot lo use:

**En `application.properties`:**
```properties
server.port=${PORT:8080}
```

O en la clase principal:
```java
@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

## ✅ Health Check

El Dockerfile incluye un healthcheck que valida:
- `GET http://localhost:8080/actuator/health`

Para que funcione, asegúrate que tengas actuator habilitado en `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

## 🐳 Prueba local con Docker

```bash
# Construir imagen
docker build -t demo:latest .

# Ejecutar contenedor
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  demo:latest

# Verificar
curl http://localhost:8080/actuator/health
```

## 🚨 Troubleshooting

### Si el contenedor no inicia
```bash
# Ver logs en Railway
railway logs

# O localmente
docker run demo:latest
```

### Error de puerto
Railway usa `$PORT` variable. Si no está configurada, Spring Boot usa `8080`.

### Si el build falla
- Verifica que tienes Java 21 en tu sistema local
- Ejecuta `./mvnw clean package` para validar que compila

## 📊 Monitoreo

Railway proporciona:
- **Logs**: En tiempo real
- **Metrics**: CPU, memoria, uptime
- **Deployments**: Historial de despliegues

Accede desde el dashboard → Project → Service → Logs/Metrics
