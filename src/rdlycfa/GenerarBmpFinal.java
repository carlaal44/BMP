package rdlycfa;

import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.util.InputMismatchException;
import java.util.Scanner;

public class GenerarBmpFinal {

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
					System.out.println("ERROR: El número debe ser >= 0.");
				}

			} catch (InputMismatchException e) {
				System.out.println("ERROR: Introduce un número entero.");
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
					System.out.println("ERROR: Valor entre 0 y 255.");
				}

			} catch (InputMismatchException e) {
				System.out.println("ERROR: Introduce un número.");
				in.nextLine();
			}

		}

		in.nextLine();
		return color;
	}

	public static void escribirCabeceraBMP(FileOutputStream fos, int tamanio) throws Exception {

		int TAMANIO_ARCHIVO = 54 + tamanio * tamanio * 3;

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

	public static void sobrescribirBMP(String archivo, int tamanioCuadrado, int rCuadrado, int gCuadrado, int bCuadrado,
			int rFondo, int gFondo, int bFondo, boolean dibujarCirculo) throws Exception {

		RandomAccessFile raf = new RandomAccessFile(archivo, "rw");

		byte[] cabecera = new byte[54];
		raf.read(cabecera);

		int ancho = (cabecera[18] & 0xFF) | ((cabecera[19] & 0xFF) << 8);
		int alto = (cabecera[22] & 0xFF) | ((cabecera[23] & 0xFF) << 8);

		int inicio = (ancho - tamanioCuadrado) / 2;
		int fin = inicio + tamanioCuadrado;

		int centro = ancho / 2;
		int radio = (tamanioCuadrado / 2) - 50;

		for (int y = 0; y < alto; y++) {

			for (int x = 0; x < ancho; x++) {

				boolean dentroCuadrado = (x >= inicio && x < fin && y >= inicio && y < fin);

				boolean dentroCirculo = ((x - centro) * (x - centro) + (y - centro) * (y - centro) <= radio * radio);

				if (dentroCuadrado) {

					long posicion = 54 + (long) (y * ancho + x) * 3;

					raf.seek(posicion);

					if (dibujarCirculo && dentroCirculo) {

						raf.write(bFondo);
						raf.write(gFondo);
						raf.write(rFondo);

					} else {

						raf.write(bCuadrado);
						raf.write(gCuadrado);
						raf.write(rCuadrado);

					}
				}
			}
		}

		raf.close();
	}

	public static void main(String[] args) {

		Scanner in = new Scanner(System.in);

		int tamanioImg, tamanioCuadrado = 0;
		int rCuadrado, gCuadrado, bCuadrado;
		int rFondo, gFondo, bFondo;
		int inicio, fin, centro, radio, opcion;

		boolean tamanioValido = false;
		boolean dibujarCirculo = false;

		String nombre;

		nombre = leerNombreFichero(in, "Nombre del archivo: ");
		tamanioImg = leerEntero(in, "Introduce el tamaño de la imagen: ");

		while (!tamanioValido) {

			tamanioCuadrado = leerEntero(in, "Introduce tamaño del cuadrado: ");

			if (tamanioCuadrado <= tamanioImg) {
				tamanioValido = true;
			} else {
				System.out.println("ERROR: El cuadrado no puede ser mayor.");
			}
		}

		System.out.println("\n---COLOR CUADRADO---");

		rCuadrado = leerColor(in, "Rojo: ");
		gCuadrado = leerColor(in, "Verde: ");
		bCuadrado = leerColor(in, "Azul: ");

		System.out.println("\n---COLOR FONDO---");

		rFondo = leerColor(in, "Rojo: ");
		gFondo = leerColor(in, "Verde: ");
		bFondo = leerColor(in, "Azul: ");

		opcion = leerEntero(in, "¿Quieres dibujar un círculo dentro del cuadrado? (1=SI / 0=NO): ");

		if (opcion == 1)
			dibujarCirculo = true;

		int sobrescribir = leerEntero(in, "¿Quieres sobrescribir un BMP existente? (1=SI / 0=NO): ");

		try {

			if (sobrescribir == 0) {

				FileOutputStream fos = new FileOutputStream(nombre);

				escribirCabeceraBMP(fos, tamanioImg);

				inicio = (tamanioImg - tamanioCuadrado) / 2;
				fin = inicio + tamanioCuadrado;
				centro = tamanioImg / 2;
				radio = (tamanioCuadrado / 2) - 50;

				for (int y = 0; y < tamanioImg; y++) {

					for (int x = 0; x < tamanioImg; x++) {

						boolean dentroCuadrado = (x >= inicio && x < fin && y >= inicio && y < fin);

						boolean dentroCirculo = ((x - centro) * (x - centro) + (y - centro) * (y - centro) <= radio
								* radio);

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

				System.out.println("BMP creado correctamente.");

			} else {

				System.out.print("Introduce BMP a modificar (ej: PORFAVOR.bmp): ");
				String bmp = in.nextLine();

				sobrescribirBMP(bmp, tamanioCuadrado, rCuadrado, gCuadrado, bCuadrado, rFondo, gFondo, bFondo,
						dibujarCirculo);

				System.out.println("BMP sobrescrito correctamente.");
			}

		} catch (Exception e) {

			System.out.println("Error al crear/modificar imagen.");
		}
	}
}
