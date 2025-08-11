package com.example.literalura;

import com.example.literalura.cliente.GutendexBook;
import com.example.literalura.cliente.GutendexClient;
import com.example.literalura.cliente.GutendexResponse;
import com.example.literalura.model.Autor;
import com.example.literalura.model.Libro;
import com.example.literalura.repository.AutorRepository;
import com.example.literalura.repository.LibroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

@SpringBootApplication
public class LiteraluraApplication implements CommandLineRunner {

	@Autowired
	private LibroRepository libroRepository;

	@Autowired
	private AutorRepository autorRepository;

	private final GutendexClient gutendexClient;

	public LiteraluraApplication(GutendexClient gutendexClient) {
		this.gutendexClient = gutendexClient;
	}

	public static void main(String[] args) {
		SpringApplication.run(LiteraluraApplication.class, args);
	}

	@Override
	public void run(String... args) throws IOException, InterruptedException {
		Scanner scanner = new Scanner(System.in);

		while (true) {
			System.out.println("\n===== MENÚ LITERALURA =====");
			System.out.println("1. Buscar libro por título y guardar");
			System.out.println("2. Listar todos los libros");
			System.out.println("3. Listar libros por idioma");
			System.out.println("4. Listar autores");
			System.out.println("5. Listar autores vivos en un año");
			System.out.println("6. Mostrar cantidad de libros por idioma");
			System.out.println("7. Mostrar estadísticas de descargas");
			System.out.println("8. Top 10 libros más descargados");
			System.out.println("9. Buscar autor por nombre");
			System.out.println("0. Salir");
			System.out.print("Seleccione una opción: ");

			String entrada = scanner.nextLine().trim();
			int opcion;

			try {
				opcion = Integer.parseInt(entrada);
			} catch (NumberFormatException e) {
				System.out.println("⚠ Debe ingresar solo el número de la opción.");
				continue;
			}

			if (opcion == 0) {
				System.out.println("👋 Saliendo...");
				break;
			}

			switch (opcion) {
				case 1 -> {
					System.out.print("Ingrese el texto de búsqueda: ");
					String query = scanner.nextLine().trim();

					try {
						GutendexResponse response = gutendexClient.buscarLibro(query);

						if (response == null || response.getResults().isEmpty()) {
							System.out.println("⚠ No se encontraron resultados para: " + query);
							break;
						}

						System.out.println("\nResultados encontrados:");
						for (int i = 0; i < response.getResults().size(); i++) {
							GutendexBook b = response.getResults().get(i);
							String idioma = (b.getLanguages() != null && !b.getLanguages().isEmpty())
									? b.getLanguages().get(0) : "desconocido";
							System.out.printf("%d. %s | Autor: %s | Idioma: %s | Descargas: %d\n",
									i + 1,
									b.getTitulo(),
									(b.getAuthors() != null && !b.getAuthors().isEmpty()) ? b.getAuthors().get(0).getNombre() : "Desconocido",
									idioma,
									b.getCantidadDescargas()
							);
						}

						System.out.print("\nSeleccione el número del libro a guardar (0 para cancelar): ");
						int seleccion = Integer.parseInt(scanner.nextLine().trim());
						if (seleccion == 0) {
							System.out.println("❌ Operación cancelada.");
							break;
						}
						if (seleccion < 1 || seleccion > response.getResults().size()) {
							System.out.println("⚠ Selección inválida.");
							break;
						}

						GutendexBook b = response.getResults().get(seleccion - 1);

						String idioma = (b.getLanguages() != null && !b.getLanguages().isEmpty())
								? b.getLanguages().get(0)
								: "desconocido";

						Autor autor = null;
						if (b.getAuthors() != null && !b.getAuthors().isEmpty()) {
							var a = b.getAuthors().get(0);
							autor = autorRepository.findByNombre(a.getNombre())
									.orElseGet(() -> autorRepository.save(
											new Autor(a.getNombre(), a.getAnioNacimiento(), a.getAnioFallecimiento())
									));
						}

						Libro libro = new Libro(
								b.getTitulo() != null ? b.getTitulo() : "Sin título",
								idioma,
								b.getCantidadDescargas(),
								autor
						);

						libroRepository.save(libro);
						System.out.println("✅ Libro guardado:\n" + libro);

					} catch (Exception e) {
						System.out.println("⚠ Error al buscar libro: " + e.getMessage());
					}
				}

				case 2 -> {
					List<Libro> libros = libroRepository.findAll();
					if (libros.isEmpty()) {
						System.out.println("⚠ No hay libros guardados.");
					} else {
						libros.forEach(l -> System.out.println("\n" + l));
					}
				}

				case 3 -> {
					System.out.print("Ingrese el idioma (ej: en, es, fr): ");
					String idioma = scanner.nextLine().trim();

					List<Libro> libros = libroRepository.findByIdioma(idioma);
					if (libros.isEmpty()) {
						System.out.println("⚠ No hay libros en ese idioma.");
					} else {
						libros.forEach(l -> System.out.println("\n" + l));
					}
				}

				case 4 -> {
					List<Autor> autores = autorRepository.findAll();
					if (autores.isEmpty()) {
						System.out.println("⚠ No hay autores guardados.");
					} else {
						autores.forEach(a -> System.out.println("\n" + a));
					}
				}

				case 5 -> {
					System.out.print("Ingrese el año: ");
					try {
						int anio = Integer.parseInt(scanner.nextLine().trim());
						List<Autor> vivos = autorRepository.findAutoresVivosEnAnio(anio);
						if (vivos.isEmpty()) {
							System.out.println("⚠ No hay autores vivos en ese año.");
						} else {
							vivos.forEach(a -> System.out.println("\n" + a));
						}
					} catch (NumberFormatException e) {
						System.out.println("⚠ Año inválido.");
					}
				}

				case 6 -> {
					System.out.print("Ingrese el idioma (ej: en, es, fr): ");
					String idioma = scanner.nextLine().trim();
					long cantidad = libroRepository.countByIdioma(idioma);
					System.out.println("📊 Cantidad de libros en idioma '" + idioma + "': " + cantidad);
				}

				case 7 -> {
					var stats = libroRepository.findAll().stream()
							.mapToInt(Libro::getDescargas)
							.summaryStatistics();

					System.out.println("\n📊 Estadísticas de descargas:");
					System.out.println("Total libros: " + stats.getCount());
					System.out.println("Promedio: " + stats.getAverage());
					System.out.println("Máximo: " + stats.getMax());
					System.out.println("Mínimo: " + stats.getMin());
				}

				case 8 -> {
					System.out.println("\n🏆 Top 10 libros más descargados:");
					libroRepository.findAll().stream()
							.sorted(Comparator.comparingInt(Libro::getDescargas).reversed())
							.limit(10)
							.forEach(l -> System.out.printf("%s (%d descargas)\n", l.getTitulo(), l.getDescargas()));
				}

				case 9 -> {
					System.out.print("Ingrese el nombre del autor: ");
					String nombre = scanner.nextLine().trim();
					autorRepository.findByNombreContainingIgnoreCase(nombre)
							.forEach(a -> System.out.println("\n" + a));
				}

				default -> System.out.println("⚠ Opción no válida.");
			}
		}
	}
}
