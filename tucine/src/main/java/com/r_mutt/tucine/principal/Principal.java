package com.r_mutt.tucine.principal;

import com.r_mutt.tucine.model.DatosEpisodio;
import com.r_mutt.tucine.model.DatosSerie;
import com.r_mutt.tucine.model.DatosTemporadas;
import com.r_mutt.tucine.model.Episodio;
import com.r_mutt.tucine.service.ConsumoAPI;
import com.r_mutt.tucine.service.ConvierteDatos;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private Scanner teclado = new Scanner(System.in);
    private ConsumoAPI consumoApi = new ConsumoAPI();
    private final String URL_BASE = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=c1c0842b";
    private ConvierteDatos conversor = new ConvierteDatos();

    public void muestraElMenu(){
        System.out.println("Por favor escribe el nombre de la serie que desea buscar");
        //Busca los datos generales de las series
        var nombreSerie = teclado.nextLine();
        var json = consumoApi.obtenerDatos(URL_BASE + nombreSerie.replace(" ", "+") + API_KEY);
        DatosSerie datos = conversor.obtenerDatos(json, DatosSerie.class);
        System.out.println(datos);

        //Busca los datos de todas las temporadas
        List<DatosTemporadas> temporadas = new ArrayList<>();
        for (int i = 1; i <=datos.totalDeTemporadas() ; i++) {
            json = consumoApi.obtenerDatos(URL_BASE + nombreSerie.replace(" ", "+") + "&Season="+i+API_KEY);
            DatosTemporadas datosTemporadas = conversor.obtenerDatos(json, DatosTemporadas.class);
            temporadas.add(datosTemporadas);
        }
        //temporadas.forEach(System.out::println);

        // Mostrar solo el titulo de los episodios para las temporadas
        for (int i = 0; i < datos.totalDeTemporadas(); i++) {
            List<DatosEpisodio> episodiosTemporada = temporadas.get(i).episodios();
            for (int j = 0; j < episodiosTemporada.size(); j++) {
                System.out.println(episodiosTemporada.get(j).titulo());
            }
        }
        //usando funcion lambda
        temporadas.forEach(t -> t.episodios().forEach(e -> System.out.println(e.titulo())));

        //convertir toda la informacion a una lista del tipo DatosEpisodio
        List<DatosEpisodio> datosEpisodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream())
                .collect(Collectors.toList());

        //top 5 de episodios
        System.out.printf("Top 5 episodios ");
        datosEpisodios.stream()
                .filter(e -> !e.evaluacion().equalsIgnoreCase("N/A"))
                .peek(e -> System.out.println("Primer filtro (N/A)" + e))
                .sorted(Comparator.comparing(DatosEpisodio::evaluacion).reversed())
                .peek(e -> System.out.println("Segundo orden (M>m)" + e))
                .map(e -> e.titulo().toUpperCase())
                .peek(e-> System.out.println("Tercer Filtro Mayúscula (m>M)" + e))
                .limit(5)
                .forEach(System.out::println);

        //convertir los datos a una lista de tipo Episodio
        List<Episodio> episodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream()
                        .map(d -> new Episodio(t.numero(), d)))
                .collect(Collectors.toList());

        //episodios.forEach(System.out::println);

        //busqueda de episodios por año
        System.out.printf("Por favor indica el año a partir del cual quieres ver los episodios");
        var fecha = teclado.nextInt();
        teclado.nextLine();

        LocalDate fechaBusqueda = LocalDate.of(fecha, 1,1);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        episodios.stream()
                .filter(e -> e.getFechaDeLanzamiento() != null && e.getFechaDeLanzamiento().isAfter(fechaBusqueda))
                .forEach(e -> System.out.printf(
                        "Temporada " + e.getTemporada()+
                                "Episodio " + e.getTitulo() +
                                "Fecha de Lanzamiento " + e.getFechaDeLanzamiento().format(dtf)
                ));
        //Busca episodios por una parte del título
        System.out.println("Por favor escriba el titulo del episodio que desea ver");
        var pedazoTitulo = teclado.nextLine();
        Optional<Episodio> episodioBuscado = episodios.stream()
                .filter(e -> e.getTitulo().toUpperCase().contains(pedazoTitulo.toUpperCase()))
                .findFirst();
        if(episodioBuscado.isPresent()){
            System.out.println(" Episodio encontrado");
            System.out.println("Los datos son: " + episodioBuscado.get());
        } else {
            System.out.println("Episodio no encontrado");
        }
        Map<Integer , Double> evaluacionesPorTemporada = episodios.stream()
                .filter(e -> e.getEvaluacion() > 0.0)
                .collect(Collectors.groupingBy(Episodio::getTemporada,
                        Collectors.averagingDouble(Episodio::getEvaluacion)));
        System.out.println(evaluacionesPorTemporada);

        DoubleSummaryStatistics est = episodios.stream()
                .filter(e -> e.getEvaluacion() > 0.0)
                .collect(Collectors.summarizingDouble(Episodio::getEvaluacion));
        System.out.println("Media de las evaluaciones: " + est.getAverage());
        System.out.println("Episodio Mejor evaluado: " + est.getMax());
        System.out.println("Episodio Peor evaluado: " + est.getMin());
    }
}
