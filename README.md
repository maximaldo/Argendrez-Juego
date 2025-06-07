# Argendrez

## Integrantes del Grupo

- Maximiliano Maldonado 
- Matías Miño   
- Thiago Fuertes  

## Descripción Corta del Juego

Argendrez es un videojuego de ajedrez en línea en 2D que permite a dos jugadores conectarse desde sus computadoras y jugar partidas respetando las reglas oficiales del ajedrez. Ademas, incorpora modos de juego innovadores que agregan nuevas piezas, movimientos especiales y mecanicas dinamicas como cartas aleatorias y peones evolucionados. El objetivo es ofrecer una experiencia nueva y creativa pero que mantenga la esencia del ajedrez clasico.

## Tecnologías Utilizadas

- **Lenguaje:** Java  
- **Motor Grafico:** LibGDX  
- **Entorno de Desarrollo:** Eclipse 
- **Plataformas Objetivo:** Escritorio (PC), con posibilidad de ampliación a Web o Movil  
- **Redes:** Conexión entre jugadores mediante programación en red  

## Estado Actual del Proyecto
Actualmente el proyecto se encuentra en una etapa inicial. Se ha realizado la configuración del proyecto con LibGDX usando el asistente Liftoff, se ha creado y organizado el repositorio en GitHub, y se están desarrollando las primeras funcionalidades del tablero y lógica de piezas. También se está implementando la estructura básica para la conexión en red entre jugadores.

## Wiki
**Enlace a la Wiki del Proyecto (Propuesta Detallada):**
[Ver la Propuesta Completa del Proyecto
aquí](https://github.com/maximaldo/Argendrez-Juego/wiki/Propuesta-del-proyecto-%E2%80%90-Argendrez)

## Instrucciones Básicas de Compilación y Ejecución
1. Requisitos previos.
  -Java JDK 8 o superior (recomendado: JDK 11).

 - Tener instalado Gradle (opcional, el wrapper gradlew viene incluido).
  
 - Un IDE compatible como Eclipse o IntelliJ IDEA.

2. Clonar el repositorio:
git clone https://github.com/maximaldo/Argendrez-Juego

cd Argendrez-juego



3. Importar el Proyecto en Eclipse.

- Abrir Eclipse.
- Seleccionar File > Import > Gradle > Existing Gradle Project.
- Seleccionar la carpeta del proyecto clonado (Argendrez-Juego).
- Finalizar la importación y esperar que se sincronicen las dependencias.
- gradlew.bat desktop:build

4. Ejecutar el juego
En Windows

gradlew.bat desktop:run
En Linux/macOS:  ./gradlew desktop:run
