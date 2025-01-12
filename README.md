# appWebCompra-TSW
Aplicación web para poder realizar una lista de la compra con la posibilidad de hacerla colaborativa. Proyecto para la asignatura Tecnologías y Sistemas Web , 4º curso de Ingeniería Informática en UCLM (Ciudad Real).

---

## Instrucciones
   1. Clona el repositorio:
  ```bash
git clone git@github.com:javicuarteroUCLM/appWebCompra-TSW.git
  ```
  Con el comando 'ls' podrás ver el contenido del directorio raíz
  ```bash
ls
  ```
  
   
    
  Los directorios que nos interesan son _backendUsers_, _backendLists_ y _frontend_.
  
  2. Abre 2 terminales adicionales a la que tienes abierta ahora. Lo puedes hacer con Ctrl + Shift + T en Windows o con Cmd + T en MacOs.
    
  3. En el terminal 1 ejecuta:
 ```bash
cd backendUsers
  ```
```bash
cd src/main/resources
```
Insertar en este directorio el archivo _config.json_ que deberá pedir a los estudiantes vía email (Javier.Cuartero.@alu.uclm.es).

Una vez añadido el archivo, ejecutar 3 veces:
```bash
cd ..
```
Luego:

```bash
mvn spring-boot:run
  ```


  4. En el terminal 2 ejecuta:
 ```bash
cd backendLists
  ```
```bash
mvn spring-boot:run
  ```

  5. En el terminal 3 ejecuta:
 ```bash
cd frontend
  ```
  Instala las dependencias
  ```bash
npm install react@18 react-dom@18 react-scripts@5
  ```
  Arranca el frontend
  ```bash
npm start
  ```
---

## Tests
Los tests con Selenium se encuetran en el backendUsers, en la carpeta test.

Mientras que los test con JMeter se encuentran en el directorio raíz.


     


        


