/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Rdf;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.SimpleSelector;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.log4j.varia.NullAppender;

/**
 *
 * @author USUARIO
 */
public class Write {

    public static void main(String[] args) throws JsonProcessingException {
        // TODO code application logic here      
        org.apache.log4j.BasicConfigurator.configure(new NullAppender());

        /*Declaracion de variables*/
//        ArrayList<String> optionsList;
        HashMap<String, ArrayList> groupsDictionary;
        groupsDictionary = new HashMap<>();
        JsonArray arrayJson;
        //Grupo
        StmtIterator iterGroup;
        Resource actualGroupResource;
        String actualGroupString;
        String groupResourceURI = "http://example.org/Group";
        ArrayList<Grupo> grupos;
        //Pregunta
        StmtIterator iterQuestion;
        Resource actualQuestionResource;
        String actualQuestionString;
        String questionResourceURI = "http://example.org/Question";
        String questionPropertyURI = "http://example.org/question";
        ArrayList<Pregunta> preguntas;
        //Opcion
        StmtIterator iterOption;
        Resource actualOptionResource;
        String optionQuestionString;
        Statement correctOption;
        String optionPropertyURI = "http://example.org/option";
        String optionCorrectPropertyURI = "http://example.org/correct";
        ArrayList<Opcion> opciones;

        /*Lectura del archivo .rdf o .ttl*/
//        String urlRead = "cuestionario.rdf";
        String urlRead = "cuestionario.ttl";
        Model model = ModelFactory.createDefaultModel();
        readRDF(model, urlRead);

        /*ITERATE GROUPS*/
        iterGroup = model.listStatements(new SimpleSelector(null, RDF.type, model.getResource(groupResourceURI)));
        grupos = new ArrayList<>();
        if (iterGroup.hasNext()) {
            while (iterGroup.hasNext()) {
                Grupo grupoObject;
                preguntas = new ArrayList<>();
                actualGroupResource = iterGroup.nextStatement().getSubject();
                actualGroupString = actualGroupResource.getProperty(RDFS.label).getObject().toString();
                //iterQuestion con actual Grupo
                iterQuestion = model.listStatements(new SimpleSelector(actualGroupResource,
                        model.getProperty(questionPropertyURI), (RDFNode) null));
                int i = 1;
                /*ITERATE QUESTIONS*/
                if (iterQuestion.hasNext()) {
                    while (iterQuestion.hasNext()) {
                        Pregunta objectPregunta;
                        opciones = new ArrayList<>();
                        actualQuestionResource = model.getResource(iterQuestion.nextStatement().getObject().toString());
                        actualQuestionString = actualQuestionResource.getProperty(RDFS.label).getObject().toString();
                        correctOption = model.getRequiredProperty(actualQuestionResource, model.getProperty(optionCorrectPropertyURI));
                        //iterOption con actual Pregunta
                        iterOption = model.listStatements(
                                new SimpleSelector(actualQuestionResource,
                                        model.getProperty(optionPropertyURI), (RDFNode) null));
                        /*ITERATE OPTIONS*/
                        while (iterOption.hasNext()) {
                            Opcion opcionObject;
                            actualOptionResource = model.getResource(iterOption.nextStatement().getObject().toString());
                            optionQuestionString = actualOptionResource.getProperty(RDFS.label).getObject().toString();
                            //Objeto Opcion
                            opcionObject = new Opcion(actualOptionResource.getURI(), optionQuestionString);
                            opciones.add(opcionObject);
                        }
                        /*Generamos el objeto Pregunta*/
                        objectPregunta = new Pregunta(actualQuestionResource.getURI(),
                                actualQuestionString, correctOption.getObject().toString(), opciones);
                        preguntas.add(objectPregunta);

                        /*Generamos el diccionario de opciones para la actual pregunta*/
                        groupsDictionary.put(actualQuestionString, opciones);
                        i++;
                    }
                }
                grupoObject = new Grupo(actualGroupResource.getURI(), actualGroupString, preguntas);
                grupos.add(grupoObject);
            }

        } else {
            System.out.println("No were found in the database");
        }
        arrayJson = new JsonArray();
        for (Grupo grupo : grupos) {
            printGrupo(grupo);
        }
        ObjectMapper mapper = new ObjectMapper();
        String jsonFromMap = mapper.writeValueAsString((Map) groupsDictionary);
//        System.out.println(jsonFromMap);
//        printModel(model);
//        model.write(System.out, "JSON-LD");    
//        model.write(System.out, "RDF/JSON");
    }
    
    public static void printGrupo(Grupo grupo){
        ArrayList<Pregunta> preguntas = grupo.getPreguntas();
        System.out.println("******** GRUPO **********");
        System.out.println(grupo.getId() + " " + grupo.getLabel());
        for (Pregunta pregunta : preguntas) {
            printPregunta(pregunta);
        }
    }

    public static void printPregunta(Pregunta pregunta) {
        ArrayList<Opcion> optionsList = pregunta.getOpciones();
        System.out.println(pregunta.getId() + ": " + pregunta.getLabel());
        for (Opcion opcion : optionsList) {
            System.out.println(opcion.getId() + ": " + opcion.getLabel());
        }
        System.out.println("Correcta: " + pregunta.getIdCorrect());
    }

    public static void readRDF(Model model, String urlRead) {
        // use the FileManager to find the input file
        InputStream in = FileManager.get().open(urlRead);
        if (in == null) {
            throw new IllegalArgumentException(
                    "File: " + urlRead + " not found");
        }

        // read the RDF/XML file
//        model.read(in, "TTL");
        model.read(urlRead, "TURTLE");
//        model.read(urlRead, "RDF/XML");
    }

}
