# 📱 FeriaLibreApp

**FeriaLibreApp** es una aplicación móvil desarrollada en Kotlin con Jetpack Compose, orientada a digitalizar la experiencia de compra y participación en ferias libres de Chile. Permite a usuarios explorar ferias activas, conocer productos y puestos, y a los feriantes gestionar su presencia en estos espacios.

## 🚀 Características Principales

- 🗺️ Mapa interactivo con ferias activas (geolocalización)
- 🛍️ Navegación jerárquica: Ferias → Puestos → Productos
- 👤 Sistema de roles (Usuario, Feriante, Administrador)
- 📝 Inscripción de feriantes a ferias disponibles
- 🧾 Registro de productos con fotos
- 🔒 Acceso autenticado con Firebase Authentication
- ☁️ Almacenamiento en la nube con Firestore y Firebase Storage

## 🧱 Arquitectura

- **Frontend:** Jetpack Compose (Kotlin)
- **Backend as a Service:** Firebase (Auth, Firestore, Storage)
- **Arquitectura recomendada:** MVVM (en transición desde pantallas monolíticas)
- **Gestión de estado:** `remember`, `mutableStateOf`


## 🧑‍💻 Instalación del Proyecto

1. Clona el repositorio:
   ```bash
   git clone https://github.com/tuusuario/FeriaLibreApp.git
