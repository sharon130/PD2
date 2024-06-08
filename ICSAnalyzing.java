import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ICSAnalyzing {
    public static void main(String[] args) {
        //String fileName = "icalexport.ics";
        String fileName = args[0];
        Map<Integer, List<String>> contentMap = new HashMap<>();
        int numEvent = 0;

        try (FileReader fr = new FileReader(fileName);
             BufferedReader br = new BufferedReader(fr)) {

            String line;
            while ((line = br.readLine()) != null) {
                line = line.replaceAll("\\s+", " ");
                line = line.trim();
                if (line.contains("BEGIN:VEVENT")) {
                    numEvent += 1;
                    line = br.readLine();

                    while (line != null && !line.equals("END:VEVENT")) {
                        if (line.contains("DESCRIPTION")) {
                            StringBuilder descriptionBuilder = new StringBuilder();
                            String[] parts = line.split(":", 2);
                            descriptionBuilder.append(parts[1]);

                            while ((line = br.readLine()) != null && !line.trim().isEmpty() && !line.startsWith("CLASS:") && !line.equals("END:VEVENT")) {
                                descriptionBuilder.append(" ").append(line.trim());
                            }

                            Generate.event("DESCRIPTION:" + descriptionBuilder.toString(), contentMap, numEvent);
                            continue;
                        }

                        Generate.event(line, contentMap, numEvent);
                        line = br.readLine();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        WriteToFile.File("output", contentMap, numEvent);
    }
}

class Generate {
    public static void event(String line, Map<Integer, List<String>> contentMap, int numEvent) {
        line = line.replaceAll("\\s+", " ");
        line = line.trim();
        List<String> contentLineList = contentMap.getOrDefault(numEvent, new ArrayList<>());
        String[] parts = line.split(":", 2);
        String contentLine;
        if(parts.length != 2){
            return;
        }
        if (parts[0].contains("SUMMARY")) {
            contentLine = "    " + "title:" + "\'" + parts[1] + "\'";
        } else if (parts[0].contains("DTSTART")) {
            String time = "";
            if (parts[1].contains("T")){
                String year = parts[1].substring(0, 4);
                String month = parts[1].substring(4, 6);
                String day = parts[1].substring(6, 8);
                String hour = parts[1].substring(9, 11);
                String minute = parts[1].substring(11, 13);
                String second = parts[1].substring(13, 15);
                time = year + "-" + month + "-" + day + "T" + hour + ":" + minute + ":" + second;
            } else {
                String[] date = parts[1].split("(?<=\\G.{2})");
                time = date[0] + date[2] + '-' + date[1] + '-' + date[2];
            }
            contentLine = "    " + "start:" + "\'" + time + "\'";
        } else if (parts[0].contains("CATEGORIES")) {
            contentLine = "    " + "class:" + "\'" + parts[1] + "\'";
        } else if (parts[0].contains("DESCRIPTION")) {
            contentLine = "    " + "description:" + "\'" + parts[1] + "\'";
        } else {
            return;
        }
        contentLineList.add(contentLine);
        contentMap.put(numEvent, contentLineList);
    }
}

class WriteToFile {
    public static void File(String title, Map<Integer, List<String>> contentMap, int numEvent) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(title + ".txt"))) {
            for (Map.Entry<Integer, List<String>> entry : contentMap.entrySet()) {
                bw.write("{");
                bw.newLine();
                List<String> eventContent = entry.getValue();
                for (int i = 0; i < eventContent.size(); i++) {
                    bw.write(eventContent.get(i));
                    if (i < eventContent.size() - 1) {
                        bw.write(",");
                    }
                    bw.newLine();
                }
                if(entry.getKey() != numEvent) {
                    bw.write("},");
                } else {
                    bw.write("}");
                }
                bw.newLine();
            }
            System.out.println("File " + title + ".txt" + " has been created successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
