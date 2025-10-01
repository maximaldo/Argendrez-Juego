## CHANGELOG

Este archivo registrará los cambios significativos realizados en el proyecto a lo largo del tiempo.


## [0.0.1] - 2025-06-02

### Agregados
Se crearon los archivos necesarios para  empezar con el proyecto como:
- Creación inicial del proyecto **Argendrez** con LibGDX.
- Configuración del repositorio en GitHub.
- Creación de `README.md`, `CHANGELOG.md` iniciales.
- Configuración Wiki del proyecto configurada con la propuesta.



---

## [0.0.2] - 2025-08-18
### Agregado
- Implementación de la clase `InputJugador.java` para manejar entradas del usuario con `InputProcessor`.
- Lógica central para seleccionar y mover piezas (`Pieza.java`, `GestorPiezas.java`).
- Pantallas principales: `MenuPantalla.java` y `JuegoPantalla.java`.
- Integración inicial de recursos gráficos (sprites de piezas, tablero, logo).
- Música de menú (`musica_menu.mp3`) en carpeta `assets`.

### Cambiado
- Mejoras en la estructura del código para separar lógica del renderizado (`Render.java`, `Recursos.java`).

### Pendiente
- Añadir HUD básico (turnos, estado del juego).
- Implementar pantalla de pausa y/o game over.
- Animaciones a partir de `piezas_spritesheet.png`.
- Sonido en ejecución dentro de las pantallas.
- Actualización del README con capturas de pantalla y video demostrativo.
