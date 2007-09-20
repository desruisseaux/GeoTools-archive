/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2007, GeoTools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package org.geotools.gui.swing.misc;

import java.io.*;
import java.util.*;


/** Classe pour la manipulation rapide de fichier.
 * @author Johann Sorel
 */
public class FastFile {

    private static InputStream ips;
//private static OutputStream out;

    /** Ignore les lignes commencant par # ou / et les lignes vides
     * @param adresse : chemin du fichier
     * @return une Arraylist avec chaine par ligne du fichier
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static ArrayList<String> Read(String adresse) throws FileNotFoundException, IOException {
        ArrayList<String> str = new ArrayList<String>();


        ips = new FileInputStream(adresse);
        InputStreamReader ipsr = new InputStreamReader(ips);
        BufferedReader br = new BufferedReader(ipsr);
        String l;
        char ch1 = '/';
        char ch2 = '#';

        while ((l = br.readLine()) != null) {
            if (l.length() > 0) {
                if (l.charAt(0) != ch1 && l.charAt(0) != ch2) {
                    str.add(l);
                }
            }
        }
        br.close();
        ips.close();


        return str;
    }

    /** Permet de recupérer un objet sérializé.
     * @param adresse : chemin du fichier
     * @return retourne l'objet contenu dans le fichier
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ClassCastException
     * @throws ClassNotFoundException
     */
    public static Object ReadObjet(String adresse) throws FileNotFoundException, IOException, ClassCastException, ClassNotFoundException {

        FileInputStream fileIn = new FileInputStream(adresse);
        ObjectInputStream in = new ObjectInputStream(fileIn);
        Object o = in.readObject();
        return o;
    }

    /** Permet de recupérer la chaine equivalante dans un fichier.
     *
     * exemple de ligne dans le fichier : nom=Robert
     * FastFile.ReadValue("x.txt","nom"); retourne "Robert"
     * @param adresse : chemin du fichier
     * @param nomparam
     * @return retourne la chaine qui correspond au nom
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static String ReadValue(String adresse, String nomparam) throws FileNotFoundException, IOException {
        String str = "";


        ips = new FileInputStream(adresse);
        InputStreamReader ipsr = new InputStreamReader(ips);
        BufferedReader br = new BufferedReader(ipsr);
        String l;
        char ch1 = '/';
        char ch2 = '#';

        //tant qu'il y a des lignes
        while ((l = br.readLine()) != null) {
            //si la ligne contient quelquechose
            if (l.length() > 0) {
                //si ce n'est pas un commentaire et si ca correspond a la variable recherche
                if (l.charAt(0) != ch1 && l.charAt(0) != ch2 && l.subSequence(0, l.indexOf("=")).equals(nomparam)) {
                    str = l.substring(l.indexOf("=") + 1, l.length());
                }
            }
        }
        br.close();
        ips.close();



        return str;
    }

    /** Permet de recupérer la chaine equivalante dans un fichier.
     *
     * exemple de ligne dans le fichier : nom=Robert
     * FastFile.ReadValue("x.txt","nom"); retourne "Robert"
     * @param flux : flux du fichier
     * @param nomparam
     * @return retourne la chaine qui correspond au nom
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static String ReadValue(InputStream flux, String nomparam) throws FileNotFoundException, IOException {
        String str = "";



        InputStreamReader ipsr = new InputStreamReader(flux);
        BufferedReader br = new BufferedReader(ipsr);
        String l;
        char ch1 = '/';
        char ch2 = '#';

        //tant qu'il y a des lignes
        while ((l = br.readLine()) != null) {
            //si la ligne contient quelquechose
            if (l.length() > 0) {
                //si ce n'est pas un commentaire et si ca correspond a la variable recherche
                if (l.charAt(0) != ch1 && l.charAt(0) != ch2 && l.subSequence(0, l.indexOf("=")).equals(nomparam)) {
                    str = l.substring(l.indexOf("=") + 1, l.length());
                }
            }
        }
        br.close();
        ips.close();


        return str;
    }

    /** Permet d'ecrire une liste de chaine dans une fichier.
     * @param adresse : adresse d'ecriture
     * @param val : liste de chaine a ecrire (une par ligne)
     * @throws IOException
     */
    public static void Write(String adresse, List<String> val) throws IOException {
        int I;


        FileWriter fw = new FileWriter(adresse, false);
        BufferedWriter output = new BufferedWriter(fw);


        for (I = 0; I < val.size(); I++) {
            output.write(val.get(I));
            output.flush();
        }

        fw.close();
        output.close();
    }

    /** Permet d'ecrire un Objet.
     * @param adresse : chemin du fichier
     * @param O : objet a ecrire
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void WriteObject(String adresse, Object O) throws FileNotFoundException, IOException {

        FileOutputStream fileOut = new FileOutputStream(adresse);
        ObjectOutputStream out = new ObjectOutputStream(fileOut);

        out.writeObject(O);
        out.close();
        fileOut.close();
    }
}