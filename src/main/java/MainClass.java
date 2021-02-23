import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;

public class MainClass {


    public static void main (String []args){
       /* System.out.println(Runtime.getRuntime().totalMemory()/1000000 + "  ..........    "+Runtime.getRuntime().freeMemory()+"......."+ (double)(Math.pow(2,10)));
        System.out.printf("%.3fGiB\n", Runtime.getRuntime().maxMemory() / Math.pow(2,30) );
        System.out.println(Runtime.getRuntime().availableProcessors());*/


        File dir = new File ("directory");


        /*File [] files = dir.listFiles();
        Arrays.stream(files).forEach(f->System.out.println(f.getName()));*/
        ForkJoinPool pool = new ForkJoinPool(64);
        FileCount fileCount = new FileCount(dir);
        Long current = System.currentTimeMillis();
        Arrays.stream(pool.invoke(fileCount)).forEach(l->System.out.print(l + " "));
        System.out.println();
        Long after = System.currentTimeMillis();
        System.out.println(after-current);
    }
}

class FileCount extends RecursiveTask<int []> {
    private File file;

    public FileCount(File file) {
        this.file = file;
    }




    public static int[] countLowerCase(File file) {
        int[] letterCount = new int[26];

        StringBuilder fileContent = new StringBuilder();
        Scanner fileReader = null;
        try {
            fileReader = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println(file.getName());
        }
        while(fileReader.hasNextLine()){
            fileContent.append(fileReader.nextLine());
        }
        for(int i=0;i<fileContent.length();i++){

            if(Character.isLowerCase(fileContent.charAt(i))) {

                try {
                    letterCount[fileContent.charAt(i) - 'a']++;
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println(fileContent.charAt(i));
                    //System.out.println(letterCount==null);


                }
            }
        }


        return letterCount;
    }

    @Override
    protected int[] compute() {

        if(file.isFile()){
            int[] letterCount = null;
            letterCount = countLowerCase(file);
            return letterCount;
        }
        else {
            File [] subFiles = file.listFiles();
            if(subFiles.length >0){
                FileCount [] fileCounts = new FileCount[subFiles.length];
                for(int i=0;i< fileCounts.length;i++){
                    fileCounts[i] = new FileCount(subFiles[i]);
                }
                for(int i=0;i< fileCounts.length-1;i++){
                    fileCounts[i].fork();
                }

                int [][] joinedLetterCount = new int[fileCounts.length][26];

                joinedLetterCount[fileCounts.length-1] = fileCounts[fileCounts.length-1].compute();
                for(int i=0;i< fileCounts.length-1;i++){
                    joinedLetterCount[i] = fileCounts[i].join();
                }

                //Integer [] joinedLetterCount = joinAllArrays(fileCounts[fileCounts.length-1].compute(), Arrays.stream(fileCounts).peek(l->l.join()).toArray(Integer[]::new));

//                System.out.println(Runtime.getRuntime().availableProcessors());
//                System.out.println(Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory());
                return joinAllArrays(joinedLetterCount);
            }
            else
                return new int[26];


        }



    }


    /*@Override
    protected int[] compute(){
        int[] letterCount = new int[26];
        int [] tempLetterCount ;
        File [] files = Arrays.stream(file.listFiles()).filter(f->f.isFile()).toArray(File[]::new);
        //System.out.println("files   "+files.length);
        File [] dirs = Arrays.stream(file.listFiles()).filter(f->f.isDirectory()).toArray(File[]::new);
        //System.out.println(dirs.length);
        if(files != null&&files.length>0) {
            for(File f:files){
                tempLetterCount = countLowerCase(f);
                letterCount = joinAllArrays(letterCount,tempLetterCount);
            }
        }

        if(dirs!=null && dirs.length>0){
            FileCount[] fileCounts = new FileCount[dirs.length];
            for(int i=0;i< dirs.length-1;i++){
                fileCounts[i] = new FileCount(dirs[i]);
                fileCounts[i].fork();
            }

            fileCounts[dirs.length-1] = new FileCount(dirs[dirs.length-1]);
            int [][] joinedLetterCount = new int[fileCounts.length][26];

            joinedLetterCount[fileCounts.length -1] = joinAllArrays(fileCounts[fileCounts.length-1].compute(),letterCount);
            for(int i=0;i< fileCounts.length-1;i++){
                joinedLetterCount[i] = fileCounts[i].join();
            }
            return joinAllArrays(joinedLetterCount);
        }
        return letterCount;

    }*/

    private int[] joinAllArrays(int[] ... args){
        int [] letterCounts = new int[26];

        Arrays.stream(args).forEach(l-> {
            for(int i=0;i<26;i++){
                letterCounts[i] += l[i];
            }
        });
        return letterCounts;
    }
}
