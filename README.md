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
6.	Las tareas pendientes se registran como issues en GitHub, con prioridad alta, media o baja.

## 4. HU16: asignar mecánico a la orden

Ingresar a **Servicios → Órdenes de servicio** (también desde **Mecánicos** o **Seguimiento**). Abrir una orden, pulsar **Asignar mecánico**, seleccionar un mecánico activo y revisar la confirmación. La carga mostrada corresponde a las órdenes no finalizadas y se vuelve a consultar después de asignar.

Desde el detalle se puede cambiar el responsable y actualizar el estado a **Pendiente**, **En proceso** o **Finalizada**. El cambio de estado registra su fecha; iniciar o finalizar requiere un responsable. Las órdenes finalizadas no permiten cambiar el mecánico.

Las pantallas usan los datos existentes de MySQL y no requieren cambios al esquema ni ejecutar nuevamente el script de creación. Para probar con los datos del script, abrir `/orden/1`, que ya cuenta con diagnóstico, cliente y vehículo.

Pruebas de las reglas de negocio: `mvn -Dtest=OrdenServicioServiceTests test`.

El proyecto todavía no implementa autenticación ni autorización; el acceso exclusivo del administrador deberá conectarse cuando se incorpore el módulo de seguridad.
