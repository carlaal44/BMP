package rdlycfa;

import java.io.FileOutputStream;
import java.util.InputMismatchException;
import java.util.Scanner;

public class GenerarBMP {

	public static String leerNombreFichero(Scanner in, String mensaje) {

		String texto = "";
		boolean valido = false;

		while (!valido) {

			try {
				System.out.print(mensaje);
				texto = in.nextLine().trim();

				if (texto.length() > 0) {
					valido = true;
				} else {
					System.out.println("ERROR: Introduce un nombre válido.");
				}

			} catch (Exception e) {
				System.out.println("ERROR: Entrada no válida.");
			}
		}

		return texto + ".bmp";
	}

	public static int leerEntero(Scanner in, String mensaje) {

		int numero = -1;
		boolean valido = false;

		while (!valido) {

			try {
				System.out.print(mensaje);
				numero = in.nextInt();

				if (numero >= 0) {
					valido = true;
				} else {
					System.out.println("ERROR: El número debe ser 1 o mayor.");
				}

			} catch (InputMismatchException e) {
				System.out.println("ERROR: Introducir un número entero.");
				in.nextLine();
			}

		}

		in.nextLine();
		return numero;
	}

	public static int leerColor(Scanner in, String mensaje) {

		int color = -1;
		boolean valido = false;

		while (!valido) {

			try {
				System.out.print(mensaje);
				color = in.nextInt();

				if (color >= 0 && color <= 255) {
					valido = true;
				} else {
					System.out.println("ERROR: El número debe ser entre 0 y 255.");
				}

			} catch (InputMismatchException e) {
				System.out.println("ERROR: Introducir un número entero.");
				in.nextLine();
			}

		}

		in.nextLine();
		return color;
	}

	public static void escribirCabeceraBMP(FileOutputStream fos, int tamanio) throws Exception {

		final int TAMANIO_ARCHIVO = 54 + tamanio * tamanio * 3;

		byte[] cabecera = new byte[54];

		cabecera[0] = 'B';
		cabecera[1] = 'M';

		cabecera[2] = (byte) (TAMANIO_ARCHIVO);
		cabecera[3] = (byte) (TAMANIO_ARCHIVO >> 8);
		cabecera[4] = (byte) (TAMANIO_ARCHIVO >> 16);
		cabecera[5] = (byte) (TAMANIO_ARCHIVO >> 24);

		cabecera[10] = 54;
		cabecera[14] = 40;

		cabecera[18] = (byte) (tamanio);
		cabecera[19] = (byte) (tamanio >> 8);

		cabecera[22] = (byte) (tamanio);
		cabecera[23] = (byte) (tamanio >> 8);

		cabecera[26] = 1;
		cabecera[28] = 24;

		fos.write(cabecera);

	}

	public static void main(String[] args) {

		Scanner in = new Scanner(System.in);

		int tamanioImg, tamanioCuadrado = 0;
		int rCuadrado, gCuadrado, bCuadrado;
		int rFondo, gFondo, bFondo;
		int inicio, fin, centro, radio, opcion;

		boolean tamanioValido = false;
		boolean dibujarCirculo = false;
		boolean dentroCuadrado = false;
		boolean dentroCirculo = false;

		String nombre;

		nombre = leerNombreFichero(in, "Nombre del archivo: ");
		tamanioImg = leerEntero(in, "Introduce el tamaño de la imagen (pixeles): ");

		while (!tamanioValido) {

			tamanioCuadrado = leerEntero(in, "Introduce el tamaño del cuadrado (pixeles): ");

			if (tamanioCuadrado <= tamanioImg) {
				tamanioValido = true;
			} else {
				System.out.println("ERROR: El cuadrado no puede ser mayor que la imagen (" + tamanioImg + " px).");
			}
		}

		System.out.println();
		System.out.println("---COLOR DEL CUADRADO---");

		rCuadrado = leerColor(in, "Rojo (0-255): ");
		gCuadrado = leerColor(in, "Verde (0-255): ");
		bCuadrado = leerColor(in, "Azul (0-255): ");

		System.out.println();
		System.out.println("---COLOR DEL FONDO---");

		rFondo = leerColor(in, "Rojo (0-255): ");
		gFondo = leerColor(in, "Verde (0-255): ");
		bFondo = leerColor(in, "Azul (0-255): ");

		System.out.println();

		opcion = leerEntero(in, "¿Quieres dibujar un círculo dentro del cuadrado? (1 = SI / 0 = NO): ");

		if (opcion == 1) {
			dibujarCirculo = true;
		}

		try {
			FileOutputStream fos = new FileOutputStream(nombre);

			escribirCabeceraBMP(fos, tamanioImg);

			inicio = (tamanioImg - tamanioCuadrado) / 2;
			fin = inicio + tamanioCuadrado;
			centro = tamanioImg / 2;
			radio = (tamanioCuadrado / 2) - 50;

			for (int y = 0; y < tamanioImg; y++) {

				for (int x = 0; x < tamanioImg; x++) {

					dentroCuadrado = (x >= inicio && x < fin && y >= inicio && y < fin);

					dentroCirculo = ((x - centro) * (x - centro) + (y - centro) * (y - centro) <= radio * radio);

					if (dibujarCirculo && dentroCirculo) {

						fos.write(bFondo);
						fos.write(gFondo);
						fos.write(rFondo);

					} else if (dentroCuadrado) {

						fos.write(bCuadrado);
						fos.write(gCuadrado);
						fos.write(rCuadrado);

					} else {

						fos.write(bFondo);
						fos.write(gFondo);
						fos.write(rFondo);

					}

				}

			}

			fos.close();

			System.out.println("Imagen creada correctamente.");

		} catch (Exception e) {

			System.out.println("Error al crear la imagen");

		}

	}

}