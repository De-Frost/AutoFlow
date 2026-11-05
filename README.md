# AutoFlow

**SC-403 Desarrollo de Aplicaciones Web y Patrones** 
/ Universidad Fidélitas 
/ Profesor: Allam Mauricio Fernández Rivera - Grupo 1

## 1. Descripción

AutoFlow reúne en una sola aplicación web las actividades de un taller automotriz: programar reservas, recibir vehículos, registrar diagnósticos, asignar trabajos a los mecánicos, controlar los repuestos y cobrar.

**Problema.** Cuando la información del taller se lleva de forma manual o en herramientas separadas, es difícil controlar cada vehículo y sus trabajos, programar citas sin conflictos de espacio y horario, y saber qué repuestos hay disponibles y cómo deben reflejarse en la factura.

**Solución.** Un sistema que acompaña al vehículo desde su reserva o llegada al taller hasta la factura final, y que da a cada tipo de usuario la información que necesita según sus funciones.

**Cliente potencial.** Talleres pequeños, medianos y centros automotrices que realizan mantenimiento, diagnóstico y reparación de vehículos.

## 2. Integrantes  

**Andrey Fabián Picado Arias**	- @AndreyKdo  
**José Adrián Arroyo Hidalgo**	- @jose-arroyo-dev  
**Sebastián Brenes Alvarado**	- @sbrenes26  
**Erick Eduardo Flores Muñoz**	- @De-Frost  

## 3. Flujo de Trabajo  

1.	La rama main es la versión estable y no se hacen commits directos sobre ella.
2.	Cada tarea se desarrolla en una rama propia, con nombre del modulo
3.	Los commits son pequeños, con mensajes descriptivos, y cada integrante los hace con su propia cuenta.
4.	Al terminar la tarea se abre un pull request hacia main. Mientras otro integrante revisa y aprueba el pull request.
5.	Los conflictos se resuelven en la rama de trabajo y no en main.

## 4. Avance del proyecto (Avance 2)

@De-Frost : H18, H19, H20.  
@jose-arroyo-dev : HU14, HU15, HU16.  
@sbrenes26 : HU8, HU9, HU10.  
@AndreyKdo : HU6, HU3 y HU5.  

## 5. Requsitos para ejecucion

### Requisitos

- JDK 21
- Netbeans con Maven
- MySQL 8

### Pasos

1. Clonar el repositorio
2. Abrir MySQL Workbench y ejecutar completo el script "scriptBD/autoflow.sql"
3. Configurar la clave de MySQL con variables de entorno, desde una terminal de windows: setx DB_PASSWORD "su_clave"
4. Ejecutar el proyecto.
5. Abrir "http://localhost:8080".


## 6. Documentación por historia

### Reservas (HU-R06, HU-R03, HU-R05)

- Muestra las reservas con filtros por fecha, estado y placa. Permite registrar una reserva sin choques de horario, y cancelarla indicando el motivo. Una reserva que ya llegó al taller no se puede cancelar.

### Recepción de vehículos (HU-R08, HU-R09, HU-R10)

- Con reserva: se busca por placa o cliente, se confirma la llegada y se registra el estado inicial del vehículo. Sin reserva: se elige el vehículo y se registra igual. Cada reserva se recibe una sola vez.

### Registrar diagnóstico (HU-R14)

- Lista los vehículos recibidos que aún no tienen diagnóstico. Se escriben las fallas y los trabajos necesarios y al guardar se pasa a generar la orden.

### Generar orden de servicio (HU-R15)

- Se elige un diagnóstico sin orden y se agregan los servicios con descripción, cantidad y costo. La orden se crea en estado Pendiente y sin mecánico.

### Asignar mecánico y estado de la orden (HU-R16, HU-R17)

- Se asigna un mecánico activo (se ve su carga de trabajo) y se cambia el estado: Pendiente, En proceso o Finalizada. No se puede iniciar ni finalizar sin mecánico, y una orden finalizada ya no se modifica.

### Inventario de repuestos (HU-R18)

- Permite registrar, modificar, buscar por código o nombre y eliminar repuestos. El código no se repite y un repuesto que ya se usó no se puede eliminar.

### Consumo de repuestos (HU-R19)

- Se elige una orden sin finalizar y un repuesto; no deja pedir más de lo que hay en inventario. Al registrar, se descuenta el stock, el repuesto queda en la orden con su precio y se guarda en el historial.

### Facturación (HU-R20)

- Lista las órdenes finalizadas que aún no tienen factura. La vista previa muestra cliente, vehículo, servicios y repuestos, y el total. Cada orden tiene una sola factura.

## 7. Referencias

- **Foto de fondo del taller:** Autor: Mike van Schoonderwalt. Fuente: [Pexels](https://www.pexels.com/photo/engine-parts-on-shelves-in-car-workshop-5506059/). Licencia: Pexels License (uso libre).
- **Prototipo de diseño:** Figma del equipo.
- **Video Evidencia Avance2:** https://ufidelitas-my.sharepoint.com/:v:/r/personal/eflores80385_ufide_ac_cr/_layouts/15/stream.aspx?id=%2Fpersonal%2Feflores80385_ufide_ac_cr%2FDocuments%2FProyecto%20-%20Desarrollo%20de%20Aplicaciones%20Web%20y%20Patrones%2FAvance%202%2FEvidencia_Avance2_G1.mp4&nav=eyJyZWZlcnJhbEluZm8iOnsicmVmZXJyYWxBcHAiOiJPbmVEcml2ZUZvckJ1c2luZXNzIiwicmVmZXJyYWxBcHBQbGF0Zm9ybSI6IldlYiIsInJlZmVycmFsTW9kZSI6InZpZXciLCJyZWZlcnJhbFZpZXciOiJNeUZpbGVzTGlua0NvcHkifX0&share=cQrsa1IIuxRbTI9gC-G6OwNfEgUC9Xup03wI-RCkj0Amgu7KCA

