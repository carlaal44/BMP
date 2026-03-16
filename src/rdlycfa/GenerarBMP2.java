package rdlycfa;

import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.util.InputMismatchException;
import java.util.Scanner;

/*
 * Programa que genera imágenes BMP escribiendo el fichero byte a byte.
 * También permite modificar un BMP existente dibujando figuras encima.
 */

public class GenerarBMP2 {

	// metodo para leer un nombre de fichero
	public static String leerNombreFichero(Scanner in, String mensaje) {

		String texto = "";
		boolean valido = false;

		while (!valido) {
			try {
				System.out.print(mensaje);
				texto = in.nextLine().trim(); // quitamos espacios
				if (texto.length() > 0) {
					valido = true;
				} else {
					System.out.println("ERROR: Introduce un nombre válido.");
				}
			} catch (Exception e) {
				System.out.println("ERROR: Entrada no válida.");
			}
		}
		return texto + ".bmp"; // añadimos la extensión bmp
	}

	// metodo para leer enteros controlando errores
	public static int leerEntero(Scanner in, String mensaje) {

		int numero = -1;
		boolean valido = false;

		while (!valido) {
			try {
				System.out.print(mensaje);
				numero = in.nextInt(); // leer número

				if (numero >= 0) {
					valido = true;
				} else {
					System.out.println("ERROR: El número debe ser >= 0.");
				}
			} catch (InputMismatchException e) {
				System.out.println("ERROR: Introduce un número entero.");
				in.nextLine(); // limpiar buffer
			}
		}
		in.nextLine(); // limpiar salto de línea
		return numero;
	}

	// metodo para leer valores RGB
	public static int leerColor(Scanner in, String mensaje) {

		int color = -1;
		boolean valido = false;

		while (!valido) {
			try {
				System.out.print(mensaje);
				color = in.nextInt(); // leer color
				if (color >= 0 && color <= 255) {
					valido = true;
				} else {
					System.out.println("ERROR: Valor entre 0 y 255.");
				}
			} catch (InputMismatchException e) {
				System.out.println("ERROR: Introduce un número.");
				in.nextLine(); // limpiar buffer
			}
		}
		in.nextLine();
		return color;
	}

	/*
	 * Metodo que escribe la cabecera del archivo BMP. La cabecera ocupa 54 bytes y
	 * define cómo es la imagen.
	 */
	public static void escribirCabeceraBMP(FileOutputStream fos, int tamanio) throws Exception {
		// tamaño total del archivo (cabecera + píxeles)
		int TAMANIO_ARCHIVO = 54 + tamanio * tamanio * 3;
		// array de 54 bytes que será la cabecera
		byte[] cabecera = new byte[54];

		cabecera[0] = 'B'; // identificador BMP
		cabecera[1] = 'M'; // identificador BMP
		// tamaño total del archivo guardado en 4 bytes
		cabecera[2] = (byte) (TAMANIO_ARCHIVO);
		cabecera[3] = (byte) (TAMANIO_ARCHIVO >> 8);
		cabecera[4] = (byte) (TAMANIO_ARCHIVO >> 16);
		cabecera[5] = (byte) (TAMANIO_ARCHIVO >> 24);

		cabecera[10] = 54; // donde empiezan los píxeles
		cabecera[14] = 40; // tamaño del bloque de información de imagen

		// ancho de la imagen
		cabecera[18] = (byte) (tamanio);
		cabecera[19] = (byte) (tamanio >> 8);

		// alto de la imagen
		cabecera[22] = (byte) (tamanio);
		cabecera[23] = (byte) (tamanio >> 8);

		cabecera[26] = 1; // número de planos de color
		cabecera[28] = 24; // 24 bits por píxel (RGB)

		fos.write(cabecera); // escribir cabecera
	}

	/*
	 * Metodo para modificar un BMP existente. Usa acceso aleatorio para escribir
	 * directamente en los píxeles.
	 */
	public static void sobrescribirBMP(String archivo, int tamanioCuadrado, int rCuadrado, int gCuadrado, int bCuadrado,
			int rFondo, int gFondo, int bFondo, boolean dibujarCirculo) throws Exception {

		RandomAccessFile raf = new RandomAccessFile(archivo, "rw");

		byte[] cabecera = new byte[54];
		raf.read(cabecera); // leer cabecera

		// obtener ancho y alto desde la cabecera
		int ancho = cabecera[18] + (cabecera[19] << 8);
		int alto = cabecera[22] + (cabecera[23] << 8);

		// calcular posición del cuadrado centrado
		int inicio = (ancho - tamanioCuadrado) / 2;
		int fin = inicio + tamanioCuadrado;

		int centro = ancho / 2; // centro de la imagen
		int radio = (tamanioCuadrado / 2) - 50; // radio del círculo

		int grosor = 50; // grosor del borde del cuadrado

		boolean dentroCuadrado;
		boolean bordeCuadrado;
		boolean dentroCirculo;

		long posicion;

		for (int y = 0; y < alto; y++) { // recorrer filas
			for (int x = 0; x < ancho; x++) { // recorrer columnas
				dentroCuadrado = (x >= inicio && x < fin && y >= inicio && y < fin);
				// detectar si el píxel está en el borde del cuadrado
				bordeCuadrado = dentroCuadrado
						&& ((x >= inicio && x < inicio + grosor) || (x < fin && x >= fin - grosor)
								|| (y >= inicio && y < inicio + grosor) || (y < fin && y >= fin - grosor));
				// ecuación del círculo
				dentroCirculo = ((x - centro) * (x - centro) + (y - centro) * (y - centro)) <= radio * radio;
				// posición del píxel dentro del fichero
				posicion = 54 + (long) (y * ancho + x) * 3;
				raf.seek(posicion); // mover puntero del fichero

				if (dibujarCirculo && dentroCirculo) {
					raf.write(bFondo);
					raf.write(gFondo);
					raf.write(rFondo);
				} else if (dibujarCirculo && dentroCuadrado) {
					raf.write(bCuadrado);
					raf.write(gCuadrado);
					raf.write(rCuadrado);
				} else if (!dibujarCirculo && bordeCuadrado) {
					raf.write(bCuadrado);
					raf.write(gCuadrado);
					raf.write(rCuadrado);

				} else {
					// no escribir nada para mantener el fondo original
				}
			}
		}
		raf.close(); // cerrar fichero
	}

	// metodo principal
	public static void main(String[] args) {

		Scanner in = new Scanner(System.in);

		int modo;
		int tamanioImg = 0, tamanioCuadrado = 0;
		int rCuadrado, gCuadrado, bCuadrado;
		int rFondo, gFondo, bFondo;
		int inicio, fin, centro, radio, opcion;
		int grosor;

		boolean tamanioValido = false;
		boolean dibujarCirculo = false;
		boolean dentroCuadrado, bordeCuadrado, dentroCirculo;

		String nombre;
		// pequeño menú inicial
		System.out.println("=================================");
		System.out.println("      GENERADOR DE IMAGEN BMP    ");
		System.out.println("=================================");

		System.out.println("0 - Crear BMP nuevo");
		System.out.println("1 - Sobrescribir BMP existente");

		modo = leerEntero(in, "Selecciona opción: "); // elegir modo de funcionamiento

		if (modo == 0) {
			// crear una imagen nueva
			nombre = leerNombreFichero(in, "Nombre del archivo: ");
			tamanioImg = leerEntero(in, "Introduce el tamaño de la imagen: ");
			// comprobar que el cuadrado no sea mayor que la imagen
			while (!tamanioValido) {

				tamanioCuadrado = leerEntero(in, "Introduce tamaño del cuadrado: ");

				if (tamanioCuadrado <= tamanioImg) {
					tamanioValido = true;
				} else {
					System.out.println("ERROR: El cuadrado no puede ser mayor.");
				}
			}
		} else {
			// sobrescribir un BMP existente
			System.out.print("Introduce BMP a modificar (ej: plantilla.bmp): ");
			nombre = in.nextLine();

			tamanioCuadrado = leerEntero(in, "Introduce tamaño del cuadrado: ");
		}
		// pedir color del cuadrado
		System.out.println("\nCOLOR CUADRADO");

		rCuadrado = leerColor(in, "Rojo (0-255): ");
		gCuadrado = leerColor(in, "Verde (0-255): ");
		bCuadrado = leerColor(in, "Azul (0-255): ");

		// pedir color del fondo
		System.out.println("\nCOLOR FONDO");

		rFondo = leerColor(in, "Rojo (0-255): ");
		gFondo = leerColor(in, "Verde (0-255): ");
		bFondo = leerColor(in, "Azul (0-255): ");

		// preguntar si se quiere dibujar círculo
		opcion = leerEntero(in, "¿Quieres dibujar un círculo dentro del cuadrado? (1=SI / 0=NO): ");

		if (opcion == 1)
			dibujarCirculo = true;

		try {

			if (modo == 0) {

				// crear el archivo BMP
				FileOutputStream fos = new FileOutputStream(nombre);

				escribirCabeceraBMP(fos, tamanioImg); // escribir cabecera

				// calcular límites del cuadrado para centrarlo
				inicio = (tamanioImg - tamanioCuadrado) / 2;
				fin = inicio + tamanioCuadrado;

				centro = tamanioImg / 2; // centro de la imagen
				radio = (tamanioCuadrado / 2) - 50; // radio del círculo

				grosor = 50; // grosor del borde del cuadrado

				// recorrer todos los píxeles de la imagen
				for (int y = 0; y < tamanioImg; y++) {

					for (int x = 0; x < tamanioImg; x++) {

						// comprobar si el píxel está dentro del cuadrado
						dentroCuadrado = (x >= inicio && x < fin && y >= inicio && y < fin);

						// detectar si el píxel está en el borde del cuadrado
						bordeCuadrado = dentroCuadrado
								&& ((x >= inicio && x < inicio + grosor) || (x < fin && x >= fin - grosor)
										|| (y >= inicio && y < inicio + grosor) || (y < fin && y >= fin - grosor));

						// ecuación del círculo
						dentroCirculo = ((x - centro) * (x - centro) + (y - centro) * (y - centro)) <= radio * radio;

						// decidir qué color escribir en cada píxel
						if (dibujarCirculo && dentroCirculo) {

							fos.write(bFondo);
							fos.write(gFondo);
							fos.write(rFondo);

						} else if (dibujarCirculo && dentroCuadrado) {

							fos.write(bCuadrado);
							fos.write(gCuadrado);
							fos.write(rCuadrado);

						} else if (!dibujarCirculo && bordeCuadrado) {

							fos.write(bCuadrado);
							fos.write(gCuadrado);
							fos.write(rCuadrado);

						} else {

							// pintar fondo
							fos.write(bFondo);
							fos.write(gFondo);
							fos.write(rFondo);
						}
					}
				}

				fos.close(); // cerrar archivo

				System.out.println("BMP creado correctamente.");

			} else {

				// modificar BMP existente
				sobrescribirBMP(nombre, tamanioCuadrado, rCuadrado, gCuadrado, bCuadrado, rFondo, gFondo, bFondo,
						dibujarCirculo);

				System.out.println("BMP sobrescrito correctamente.");
			}

		} catch (Exception e) {

			System.out.println("Error al crear/modificar imagen.");
		}
	}
}