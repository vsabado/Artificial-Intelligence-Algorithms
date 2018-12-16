import javax.rmi.ssl.SslRMIClientSocketFactory;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

class Game {

    static double[] evolveWeights() {
        // Create a random initial population
        Random r = new Random();
        Matrix population = new Matrix(100, 299);
        for (int i = 0; i < 100; i++) {
            double[] chromosome = population.row(i);
            for (int j = 0; j < chromosome.length; j++) {
                if (j == 291) { //Chance winner is promoted.
                    chromosome[j] = 0.90;
                } else if (j == 292) { //Chance of mutation
                    chromosome[j] = 0.60;
                } else if (j == 293) { //Random Deviation of each mutation
                    chromosome[j] = 0.1;
                } else if (j == 294) { //Random number of mate
                    chromosome[j] = ThreadLocalRandom.current().nextInt(3, 6);
                } else {
                    chromosome[j] = 0.03 * r.nextGaussian();
                }
            }
        }
        GeneticAlgorithm genAlg = new GeneticAlgorithm(population);
        List<Double> fitnessCopy = new ArrayList<>(genAlg.fitnessValues);
        while(genAlg.numberOfWins < 3){
            genAlg.update();
        }

//        ArrayList<IAgent> agents = new ArrayList<IAgent>();
//        for (int i = 0; i < 100; i++) {
//            double[] chromosome = population.row(i);
//            agents.add(new NeuralAgent(chromosome));
//        }
//
//        try {
//            Controller.doTournament(agents);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        double highest = Integer.MIN_VALUE;
        int index = Integer.MAX_VALUE;
        for(int i = 0; i < genAlg.fitnessValues.size(); i++){
            if(genAlg.fitnessValues.get(i) > highest){
                highest = genAlg.fitnessValues.get(i);
                index = i;
            }
        }

        // Return an arbitrary member from the population
        return population.row(index);
    }


    public static void main(String[] args) throws Exception {
//        System.setOut(new PrintStream(new FileOutputStream("log_file.txt")));
//        double[] w = evolveWeights();
        //System.out.print("Winning weights: " + Arrays.toString(w));
        //Controller.doBattle(new ReflexAgent(), new NeuralAgent(w));

        //Winner1 Test
        double[] winner1 = {0.06583239724916437, 0.011043141523275818, 0.13997311176262522, 0.1881313611315007, -0.02840434457376921, -0.24011569110151, 0.09658215107792177, 0.007233415502852603, -0.042544445863434255, 0.22298119954819903, 0.06324993376761787, -0.17928349715794054, 0.6548007088207118, 0.061674957770408406, -0.15746018377331183, 0.013580975432690698, -0.13255057877964022, -0.0272533297043368, 0.1862749097516121, 0.044616079767632995, 0.10563391437332639, -0.1984048348855913, 0.022915952895627825, 0.010055435479299579, -0.5930109688285609, -0.062424425504001534, 0.13193592554860883, 0.014903092481767598, -0.004724879714947977, -0.04387048587377427, 0.015096378555727484, -0.784168942823145, 0.15830791915794507, 0.11123697476515652, 0.010690446211635188, -0.030201959905324417, 0.03886625748400109, -0.16930234687819615, -0.02779545851134499, -0.044127238299768175, -0.009295500052215536, -0.017820858668427305, 0.31770184784685573, -0.23816065148753407, 0.3392701102276211, -0.053921068130454435, 0.023505549977938394, 0.23231697285983216, -0.09910991410251506, 0.02647425946741901, -0.0711824818733507, 0.015442358268026347, -0.19314546189159287, 0.05160221529410944, 0.01063533670629525, -0.058222103966493605, -0.021059633972827542, 0.024687585164551933, 0.0016419513097657228, 0.11598020349625054, 0.0486843729436873, -0.13840309121974623, 0.18039410548412604, -0.29893922315327437, 0.023176475078620005, 0.009261167333405183, -0.17772345753934998, 0.010963097287452934, -0.019615599203903017, -0.05641915899699232, -0.008116809763536875, -0.16858166778447153, -0.02152959801551172, -0.008444244191160591, -0.2874232752430603, 0.10954170664823139, 0.20316319257117926, -0.025446205063618887, -0.2963015937325935, -0.15326173708484508, 0.07429639206374744, -0.03758888756198699, 0.025383086279550125, -9.092310454322406E-4, 0.17213781766006397, -0.03143013189484433, -0.02758484790756792, -0.05526419658534457, 0.11966820179753329, 0.27326170470203176, -0.005127548820194939, 0.02465580580060385, -0.2348119508825407, 0.015599822730367913, -0.0837549075361157, 0.09661542261185212, 0.16259545179334778, -0.014808587572596073, 0.34066322731949783, -0.04862765028034409, -0.013019204781906072, -0.2272334988035495, -0.03074291767269921, 0.17259323175735597, -0.036520262592151485, -0.025198218896891406, 0.09089040271990986, -0.021382618155167496, -0.1296501981055052, -0.06620421086636227, -0.038701658177706356, 0.09874748536747908, -0.051923034649265114, -0.0032083193653905163, 0.20199015480000831, 0.1823821711803249, -0.14534923005482375, -0.02020080629149258, -0.06849372756884634, -0.005555269145015398, -0.02636913333417522, -0.16121212517081382, 0.005590927850110755, 0.028007807778464566, 0.012231251206954362, 0.07309078313333892, 0.03492722325399364, -0.16423695169160318, 0.21456974226563724, -0.017390281929769283, -0.0746403939397311, 0.042301086347840244, -0.40974097581597035, -0.018565543023179348, -0.2718860354693225, 0.1830476972747832, -0.034891553791984, 0.0837890191440383, -0.011652512282436215, 0.0025139683670546116, 0.2538983057809441, 0.22743000956649678, -0.33496800870317267, -0.2508242403602061, 0.08414002899164573, -0.03172336972429859, 0.039019550795066305, -0.12715761731726094, -0.0058969705951963545, -0.04500220752067198, -0.11162054510116909, -0.20716350205325618, -0.023475323075335553, 0.049033950881797794, -0.15382327222022968, -0.0523961479011492, -0.010756046272326727, 0.009044469044664414, -0.12416897498847598, -0.05315732632221712, 0.13633095958368915, 0.38009672771554176, 0.2295234535166774, -0.028250267602881577, -0.03695299438634602, 0.09536013519223467, -0.34915729352500946, 0.017653194742711372, -0.12048243440729006, 0.10441153887701397, 0.0475163073559406, 0.12568619491821004, 0.06143065319538928, 0.11698395496967867, -0.007186711102509732, -0.03586608689933995, -0.036073044443487905, 0.027848850031176636, -0.0031109745884249093, -0.002270952150044894, 0.16155964198845363, -0.039531083650602754, -0.0536656506813284, -0.0021605819513062993, 0.022394666201258615, -0.0202884436003773, 0.10104940744715213, -0.0854418042545759, 1.5354971020563205E-4, 0.03118713405555855, -0.14447310607161404, -0.2899741723525442, 0.0014891597873925855, -0.061209040440111905, -0.012956196544120252, -0.07353753357888715, 0.04888417576886549, -0.01712928919054988, -0.0580044605469866, -0.06517687210118847, 0.1560320116901915, 0.008866383934136846, 0.059686284596457126, 0.014611328144415712, 0.024459388480139455, -0.5572641546874273, 0.09480699271815438, 0.20529176098816512, 0.2061460750591671, 0.07540599803483089, 0.02463972036040705, 0.0037551453693945754, -0.0200961267114676, 0.007410766393144629, -8.504400237201759E-4, 0.19681073450974354, -0.01647986773878827, 0.014147574423159767, 0.06383967082712796, -0.006004214679097922, -0.2873195971319452, -0.04227038359654042, 0.26912554584364873, 0.029167982478051954, -0.01653975238063074, 0.10973409034731671, -0.07091612043620792, 0.01523701394295391, 0.21241570197361234, -6.447187468914913E-4, 0.0500648021896216, -0.18392223107665467, -0.2584725812211178, -0.022883398339667342, 0.15730003297179118, 0.048699858800721314, 0.30396513777624196, -0.015601524939685233, 0.23818463051473418, 0.011833339580509314, -0.035749084385492305, -0.03800667131799589, 0.09825247736810236, -0.015607530738411183, 0.023430134642582805, 0.022035501583404202, -0.020335345239005296, 0.0018329120871220569, 0.0678521249330345, -0.035710357963628994, -0.03237669450443017, -0.03762794772128113, 0.07260300598406595, -0.014308913543437638, -0.11690993789499607, -0.037984874609658076, -0.025960309319715658, 0.06212326542586069, -0.09591283293192682, 0.14404012411104453, 0.05264492912178771, 0.2657712057336987, -0.05174709275751576, -0.13438284674246204, -0.10849302422149801, -0.022871185261253514, -0.05835275663959182, -0.14693826836832038, 0.647636175272325, 0.002578299732075854, -0.12738436049705124, -0.06804280456803077, -0.24426531029903376, 0.18220962837547297, 0.1750320956267538, 0.023105051071711555, -0.07242174412178211, -0.046638010598906474, -0.330924833530152, -0.034114985615491235, 0.060059052944325717, -7.39307695231504E-4, -0.15034598498743632, -0.021311962763091483, -0.21262324368596286, -0.25385231123299273, 0.11785338164714333, -0.1122808734314232, 0.03008778090387284, -0.047012175103566325, 0.02280579431922202, 0.5698181135458651, 0.6593871278044166, 0.13775106428489434, 3.0, -0.04241365709173546, -0.026013872676585205, 0.0058353542791813795, -0.015143413015846067};
        //double[] winner2 = {0.2689436547709233, -0.10801002475009139, 0.22158495384514745, 0.019587023945649512, 0.008861875085268887, -0.10461564387027347, -0.057979127911078315, 0.020355275864126523, -0.00198952770829707, -0.3507841408970561, 0.027777073834805075, 0.0012892464685712217, 0.044970331785624464, 0.01291702385209998, -0.007494777722083359, 0.028254965852351398, -0.1396929563524788, -0.04184970778328655, -0.01860371841567568, 0.13264893804198913, -0.21039286835088133, 0.05574068069199476, -0.07999928315794061, -0.0723117354130955, 0.13790106993736612, -0.13654132056471405, 0.18914186503394056, 0.039889164675952916, -0.01309111287268843, 0.03707250503847555, -0.06760794100782949, 0.09721413717199028, 0.021598096555954068, 0.26863697404684, -0.4317698847125537, -0.013834616859536006, -0.10921954316411626, 0.1506810483765546, -0.029542567401623913, 0.04584528705779634, -0.1636825238236937, -0.16357082097612113, 0.03112030058967416, 0.23072085207752582, -0.009142810292331244, 0.04519253101581007, 0.054653634616874155, 0.07406485298256936, 0.03858812112614506, -0.0703133031604311, -0.02431628668342062, 0.05263596869071494, 0.24355786610234878, -0.16489418664488803, 0.16228651826551102, -0.18047383135243733, -0.13605643522232186, -0.04053925218038969, -0.17812133470055455, -0.06022649834412452, 0.0649209512047999, -0.008931141083520505, -0.0677364136022832, 0.11815794937388088, -0.05736166000329327, 0.010897316200028389, -0.00454246387931994, -0.06394565689070489, 0.25047821418125715, -0.020060507611925252, -0.0693972202516983, -0.25383059685694753, -0.062102631786052014, -0.009382521216980745, -0.2726722585354748, 0.0943822987756754, -0.010393191700290903, -0.20221961008449463, 0.07193323296057383, -0.02107166558696849, -0.057466628469832255, -0.01613139856399898, -0.12071363380464599, -0.04920908496600504, 0.03707679540265807, 0.25230292914858266, 0.11921350074458822, -0.0021010559831560424, -0.010887124284054446, 0.05455034536439764, -0.2445414101505196, 0.2836521264511189, -0.1274986527110956, -0.05060745784475813, 0.18009873375477023, -0.011293453086576793, -0.24716738817935602, 0.003759353458328059, 0.053998371574279475, 0.05302991037619725, 0.1425381114201094, -0.05188441329828834, 0.023773614850419667, -0.029887012067122853, 0.07422645341910386, -0.058552449544778826, -0.0408915740770918, 0.22959255696940228, -0.05001353155269324, -0.01847065285655397, -0.02188301602593358, 0.020833333263759385, -0.008118274543681877, -0.34019929356312884, -0.1062364918053926, -0.035672052882602404, 0.03967160669842185, -0.06838689239327006, 0.06871755757361801, -0.11717598507147216, 0.08052695127461566, -0.007029844296673212, 0.08215877886329967, 0.21435383511797232, -0.03435000252029259, -0.022269476044383153, -0.04619149079629899, 0.02999376119941015, 0.055887771507348645, -0.15238886839537472, -0.14474517545955226, 0.42071183518145716, 0.19654801154822493, -0.021505498349603528, -0.08693086069827116, -0.03510406321518787, 0.04998756776093691, 0.12135679641295272, 0.0031349988376548747, 0.01676414208389067, 0.048176839061206386, -0.02752614065432324, -0.19274982908199095, 0.27055568827756826, -0.10500470770328013, 4.124081769633994E-4, -0.236571679734467, 0.02555169114966359, 0.07363908477015949, -0.0038179013773393944, -0.030216956270366843, -0.09058921381863519, -0.049812834319555716, 0.19418444556509887, -0.03657999811869082, -0.011815116284918808, -0.02190440313833429, -0.073775858324878, 0.04106146011217726, -0.027746966671940407, -0.14953268971981054, 0.028278001447932542, 0.06560310836664103, 0.12362534553697886, -0.03246944558579501, 0.009964411106828542, -0.02005031990488266, -0.03732861124729718, 0.0029905988715524284, -0.005750628826665876, 0.010806504490272876, -0.025992755970183384, -0.0742232223376637, -0.05349107357309282, -0.09699188802960254, 0.062018435806271914, -0.18467343115777327, 0.04468560019149164, 0.011763112896635931, -0.02182200427725618, 0.15050857330806172, -0.27785326056553894, 0.08757163663341691, -0.013961270070007721, 0.1104626429998779, 0.05438823867863299, 0.0723423322896535, 0.04875870154539072, 0.1920568430049225, -0.13883918648009247, -0.056786930793284174, -0.009534213374674784, 0.257379717441138, -0.23188503745812888, -0.1114385087111536, -0.10222767948678639, -0.04960139713253916, 0.11837065763866528, -0.14763896170761068, 0.02798947755381862, 0.053784137463072736, -0.38957174605410144, 0.2361617811757482, -0.09995559796609595, 0.21109316009283047, 0.3688082117632065, 0.04163551966311998, 0.19593618236273036, 0.12167788120752936, -0.10650049507590031, 0.017378544473636588, 0.08628562897155038, 0.21097620474604445, 0.004842625091346696, -0.01892990851158132, 0.11084418208632588, 0.018196652317072726, 0.04837981783197169, 0.07030718025389299, 0.10540880609988484, 0.012400240796945794, 0.031330000144307465, -0.04743390624803916, 0.059984310004458176, 0.31078146822131575, 0.10051142252998581, -0.002879200852462373, -0.1401925124461936, 0.4195892487698807, 0.12073647395625901, -0.3113295756163869, 0.06748800333153901, 0.029346131636137925, 0.09250524139068624, -0.31927232774145503, -0.2305207303822771, -0.04322744852654473, 0.18747427503972652, -0.055802318839907036, 4.0578774319616613E-4, 0.14346879817313768, -0.23853267191043004, -0.031170980997107717, -0.13180479393183464, -0.10215904274182233, 0.10163944346210127, 0.00746841834248482, 0.3145806008408971, -0.027913962963171616, -0.06730455797101398, -0.0047734327667450435, -0.09729195294342743, -0.03787706494108475, -0.27004697976886405, -0.002328043739051598, -0.09519693569231288, -0.0738907770789001, 0.05419448637635217, -0.21957655545388857, 0.01977059021750914, 0.02076251706167314, 0.01726629337466824, 0.10927693904325701, 0.07350139352884619, 0.11326774380151912, 0.16082192284672958, 0.01054346093693255, 0.22032479218984596, -0.1969733032508316, -0.04778922094255224, -0.07445115002792041, 0.1762871369287863, 0.032105729889411676, -2.148133819636953E-4, -0.10536066944186082, -0.10285827995860941, 0.019805547085910036, 0.2005689350221796, 0.002886378411992354, -0.11377942692242626, -0.0780548219754058, 0.08777622505877844, 0.017508626103652017, -0.006606279859342965, -0.12543528340525242, -0.026663326374244914, -0.03241524813017902, -0.009703091436864627, 0.022623092665728466, 0.014000598353899841, -0.05522985916330812, 0.5736617285574189, 0.6, 0.11871827675991468, 5.0, 0.11945058467302586, -0.08189342955504281, 0.019000461411122403, -0.035410153277645975};
        Controller.doBattle(new ReflexAgent(), new NeuralAgent(winner1));


    }

}