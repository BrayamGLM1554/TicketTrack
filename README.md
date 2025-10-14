# 🚚 TicketTrack

**TicketTrack** es una aplicación móvil desarrollada para la gestión y control de presupuestos de viajes de transporte de carga.  
Permite al **consignatario** (administrador) asignar viajes, gestionar presupuestos, revisar gastos y controlar el flujo financiero de cada trayecto.  
Por su parte, los **transportistas** pueden registrar tickets, facturas y avances del viaje, todo en una interfaz simple y optimizada.

---

## 📱 Características principales

### 👨‍💼 Consignatario
- Visualización de **viajes activos** y sus presupuestos.
- Creación y asignación de **nuevos viajes**.
- Aprobación o rechazo de **gastos registrados**.
- Reporte general de **gastos mensuales** y **saldo disponible**.
- Visualización de **transportistas** y las unidades asignadas.

### 🚛 Transportista
- Registro de **tickets o facturas** de gastos por viaje.
- Consulta de **presupuesto disponible**.
- Reporte de **averías o siniestros**.
- Solicitud de **aumento de presupuesto** durante el viaje.

---

## 🧩 Estructura general de la aplicación

- **Login** → Acceso con roles (consignatario / transportista).  
- **Registro** → Alta de nuevas empresas transportistas.  
- **Dashboard (Consignatario)** → Cards, gráficos y resumen de gastos.  
- **Viajes** → Listado y detalles completos de viajes.  
- **Detalle del viaje** → Información de transportista, unidad y presupuesto.  
- **Notificaciones** → Seguimiento de eventos (tickets, inicios de viaje, reportes, etc).

---

## ⚙️ Tecnologías utilizadas

- **Frontend móvil:** Kotlin con **Jetpack Compose**
- **Backend:** Node.js con Express  
- **Base de datos:** Firebase / Firestore  
- **Autenticación:** Firebase Auth  
- **Hosting del backend:** Render / Railway (según ambiente)  
- **Control de versiones:** Git + GitHub  

---

## 🏗️ Estándares de desarrollo

### 🧠 Nomenclatura y formato
- **Clases y Componentes:** `PascalCase` → `ViajeCard.kt`, `TransportistaService.js`
- **Variables y funciones:** `camelCase` → `getActiveViajes()`
- **Constantes:** `UPPER_SNAKE_CASE`
- **Archivos:** nombre claro y descriptivo según función → `login_screen.kt`, `viajes_controller.js`

### 📦 Ramas principales
- `main` → versión estable y lista para producción.  
- `develop` → integración y pruebas de nuevas funciones.

### 🗒️ Convención de commits
- `feat:` → nueva funcionalidad  
- `fix:` → corrección de errores  
- `refactor:` → cambios internos sin alterar lógica  
- `docs:` → cambios en documentación  
- `style:` → ajustes visuales o de formato  
- `test:` → adición o corrección de pruebas  


---

## 💾 Instalación y ejecución

### Requisitos
- Node.js v18 o superior  
- Android Studio (última versión)
- Cuenta en Firebase configurada con Auth y Firestore  

### Pasos
1. Clonar el repositorio  
   ```bash
   git clone https://github.com/usuario/tickettrack.git

👥 Autores

Proyecto académico: TicketTrack
Universidad UTTT — Arquitectura de Software
Equipo de desarrollo:

Brayam Gilberto López Morales (@BrayamGLM1554) Scrum Master
Alan Uriel Damian Garcia (@) Desarrollador Back-End
Diana Guadalupe Viveros Migueles (@DianitaVv) Desarrollador Front-End
Yafte Aram Mercado Meza (@yafAram)

[Agregar demás integrantes]

🧾 Licencia

Este proyecto se distribuye bajo la licencia MIT.
Eres libre de modificar y usar el código con fines educativos o de desarrollo interno.
