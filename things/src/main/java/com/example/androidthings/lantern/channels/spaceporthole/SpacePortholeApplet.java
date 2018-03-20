package com.example.androidthings.lantern.channels.spaceporthole;

import java.util.ArrayList;
import processing.core.*;
import processing.data.*;

import processing.core.PApplet;
import java.util.*;

public class SpacePortholeApplet extends PApplet {

    ArrayList<Star> stars;

    ArrayList<Star> bigStars;
    ArrayList<Star> mediumStars;
    ArrayList<Star> smallStars;

    ArrayList<int[]> constellationLines;

    float latitude = 51.2f;
    float longitude = -0.1f;

    boolean firstDraw = true;

    public void setup() {
        orientation(LANDSCAPE);

        // increase the fov to 90deg
        perspective(radians(90), width/height, 1.0f, 1000.0f);

        // set up the camera, pointing along the Y axis (towards lat/long 0), using Z as 'up'
        camera(
                0, 0, 0,
                0, 1, 0,
                0, 0, 1);
    }

    public void draw() {
        background(0);

        if (firstDraw) {
            firstDraw = false;
            return;
        }

        if (stars == null) {
            loadStarData();
            classifyStarsBySize();
        }

        // Setup the scene.
        float rightAscension = longitude + greenwichSiderealTimeInHours()/24f*360f;
        rotateX(radians(latitude));
        rotateZ(radians(-rightAscension));

        //float mouseYRot = map(mouseY, 0, height, -PI, PI);
        //float mouseZRot = map(mouseX, 0, width, -PI, PI);
        //rotateX(mouseYRot);
        //rotateZ(mouseZRot);
        //println("mouseYRot", mouseYRot);
        //println("mouseZRot", mouseZRot);

        noFill();

        // draw dots at the star locations
        strokeCap(ROUND);

        // big stars first
        strokeWeight(6);
        beginShape(POINTS);
        for (Star star : bigStars) {
            stroke(255, map(star.magnitude, 0.66f, 1.0f, 200, 255));
            float[] location = star.renderPosition;
            vertex(location[0], location[1], location[2]);
        }
        endShape();

        // medium stars
        strokeWeight(4);
        beginShape(POINTS);
        for (Star star : mediumStars) {
            stroke(255, map(star.magnitude, 0.33f, 0.66f, 100, 255));
            float[] location = star.renderPosition;
            vertex(location[0], location[1], location[2]);
        }
        endShape();

        // small stars
        strokeWeight(3);
        beginShape(POINTS);
        for (Star star : smallStars) {
            stroke(255, map(star.magnitude, 0, 0.33f, 0, 200));
            float[] location = star.renderPosition;
            vertex(location[0], location[1], location[2]);
        }
        endShape();

        // draw lines for the constellations
        stroke(255, 80);
        strokeWeight(1);
        beginShape(LINES);
        for (int[] line : constellationLines) {
            int startStarI = line[0];
            int endStarI = line[1];

            float[] startStarLocation = stars.get(startStarI).renderPosition;
            float[] endStarLocation = stars.get(endStarI).renderPosition;

            vertex(startStarLocation[0], startStarLocation[1], startStarLocation[2]);
            vertex(endStarLocation[0], endStarLocation[1], endStarLocation[2]);
        }
        endShape();
    }

    public void loadStarData() {
        JSONObject starsDb = loadJSONObject("stars.json");

        JSONArray jsonStars = starsDb.getJSONArray("stars");
        stars = new ArrayList<Star>(jsonStars.size());

        for (int i = 0; i < jsonStars.size(); i++) {
            JSONArray jsonStar = jsonStars.getJSONArray(i);
            stars.add(new Star(jsonStar.getInt(0), jsonStar.getFloat(1), jsonStar.getFloat(2)));
        }

        JSONArray jsonConstellationLines = starsDb.getJSONArray("constellationLines");
        constellationLines = new ArrayList<int[]>(jsonConstellationLines.size());

        for (int i = 0; i < jsonConstellationLines.size(); i++) {
            JSONArray jsonConstellationLine = jsonConstellationLines.getJSONArray(i);
            constellationLines.add(new int[] { jsonConstellationLine.getInt(0), jsonConstellationLine.getInt(1) });
        }
    }

    public void classifyStarsBySize() {
        bigStars = new ArrayList<Star>(stars.size());
        mediumStars = new ArrayList<Star>(stars.size());
        smallStars = new ArrayList<Star>(stars.size());

        for (Star star : stars) {
            if (star.magnitude > 0.66f) {
                bigStars.add(star);
            } else if (star.magnitude > 0.33f) {
                mediumStars.add(star);
            } else {
                smallStars.add(star);
            }
        }
    }

    class Star {
        float magnitude;
        float rightAscension;
        float declination;
        float[] renderPosition;

        Star(int mag, float ra, float dec) {
            magnitude = map(mag, 6.0f, -2.0f, 0.0f, 1.0f);
            rightAscension = ra;
            declination = dec;

            // spherical to cartesian coords
            float r = 200f;
            renderPosition = new float[] {
                    r*cos(-dec)*sin(ra),
                    r*cos(-dec)*cos(ra),
                    r*sin(-dec)
            };
        }

        public String toString() {
            return String.format("<Star magnitude=%f, ra=%f, dec=%f>", magnitude, rightAscension, declination);
        }
    }

    Date j2000 =
            (new GregorianCalendar.Builder())
                    .setTimeZone(TimeZone.getTimeZone("GMT"))
                    .setDate(2000, Calendar.JANUARY, 01)
                    .setTimeOfDay(12, 0, 0)
                    .build()
                    .getTime();

    public float greenwichSiderealTimeInHours() {
        Date now = new Date();
        long millisSinceJ2000 = now.getTime() - j2000.getTime();
        double daysSinceJ2000 = millisSinceJ2000/1000.0d/60.0d/60.0d/24.0d;

        // ref: http://aa.usno.navy.mil/faq/docs/GAST.php
        return (float)((18.697374558d + 24.06570982441908d*daysSinceJ2000) % 24.0d);
    }
    public void settings() {  size(720, 720, P3D); }
}

