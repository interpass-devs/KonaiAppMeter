package com.konai.appmeter.driver.setting;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.konai.appmeter.driver.struct.gpspoint;
import com.konai.appmeter.driver.util.ConnectionHtml;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import static android.content.Context.MODE_PRIVATE;

public class Suburbs {

////////////////////
//20210325
    public static ArrayList<gpspoint> msuburbpoint  = new ArrayList<gpspoint>();; // 대기지역 좌표
    public static boolean mSuburbOK = true;

//20210325
    public static void suburbpoint_add(double x, double y)
    {

        gpspoint xy = new gpspoint(x, y);
        msuburbpoint.add(xy);
    }

    public static boolean Readpoint_suburb(){

        String sdata;
        File saveFile = null;
//20220503 tra..sh        if( Build.VERSION.SDK_INT < 29) saveFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/appmeter");
///        else saveFile = Info.gMainactivity.getExternalFilesDir("/appmeter");
        saveFile = Info.gMainactivity.getExternalFilesDir("/appmeter");
        StringBuffer strBuffer = new StringBuffer();
        try{
            BufferedReader reader = new BufferedReader(new FileReader(saveFile + "/suburb.txt"));
            String line="";
            while((line=reader.readLine())!=null){
                strBuffer.append(line+"\n");
            }

            reader.close();
            Info._displayLOG(Info.LOGDISPLAY, "시경계 읽기", saveFile + " " + Build.VERSION.SDK_INT);
        }catch (IOException e){
            e.printStackTrace();
            Info._displayLOG(Info.LOGDISPLAY, "시경계 읽기실패", e.toString() + " " + Build.VERSION.SDK_INT);
            return false;
        }

        String[] splitDatas = strBuffer.toString().split(";");

        if(splitDatas.length > 3) //20220520
        for(int i=0; i<splitDatas.length; i++) {

            String[] unitData = splitDatas[i].split(",");

            suburbpoint_add(Double.parseDouble(unitData[0]), Double.parseDouble(unitData[1]));
        }

        mSuburbOK = true;

        Log.d("Suburbs","시경계read " + splitDatas.length + " ver" + Info.APP_SUBURBSVER);
        return true;
    }

    public static void Savepoint_suburb(String sdata) {

        //test appmeter

        if(Info.REPORTREADY)
            Info._displayLOG(Info.LOGDISPLAY, "saving suburb" + " : " + sdata, "");

        File saveFile = null;
//20220503 tra..sh
//        if( Build.VERSION.SDK_INT < 29)
//            saveFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/appmeter");
//
//        else
//            saveFile = Info.gMainactivity.getExternalFilesDir("/appmeter");

        saveFile = Info.gMainactivity.getExternalFilesDir("/appmeter");

        try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(saveFile + "/suburb.txt", false));

            buf.write(sdata);
            buf.flush();
            buf.close();
            Info._displayLOG(Info.LOGDISPLAY, "시경계 저장", saveFile + " " + Build.VERSION.SDK_INT);
        } catch (IOException e) {
            e.printStackTrace();

            Info._displayLOG(Info.LOGDISPLAY, "시경계 저장 실패", e.toString() + " " + Build.VERSION.SDK_INT);
        }

        Log.d("Suburbs","시경계save " + "ver" + Info.APP_SUBURBSVER);
    }

    public static void point_suburbtmp()
    {
        String tmp;
//인천.
// String tmp = "126.62944249764311,37.499632518412234;126.60313065796478,37.513650344486884;126.59665587047718,37.548579010391656;126.63985902906148,37.58548082824081;126.59156455632241,37.593011823985485;126.62581676367968,37.602682107868596;126.6512526639701,37.63798791865732;126.67242225512966,37.63384201577993;126.72566531581052,37.59184225525884;126.79369544519706,37.58159385885768;126.7662043574505,37.554242375748366;126.76027622579264,37.51591576136998;126.74235365745886,37.48694907503676;126.77845499939897,37.46207684428783;126.7785287726508,37.46203559089818;126.77923966572729,37.45164188169722;126.77902288729182,37.45102090644409;126.77090454544626,37.43094416277482;126.77083792139524,37.43073681082609;126.7544652746883,37.41757293948264;126.72114288972793,37.38250405568312;126.69505736740857,37.382728422176854;126.66344966004381,37.35053506629968;126.60996708541704,37.38718717658724;126.61166364956264,37.42993401634955;126.59516762247304,37.470751125354276;126.62944249764311,37.499632518412234;126.54063203734842,37.52137210621049;126.58257529425838,37.490646554011505;126.50766160048013,37.466180988746544;126.44308783596472,37.42152563638588;126.38005400083568,37.43990787098436;126.35578510553542,37.46756799120455;126.41691275707815,37.496070547623596;126.47134884661521,37.49847651609366;126.49426957115062,37.507495537162704;126.51237343504722,37.5342594061903;126.54063203734842,37.52137210621049;126.10489742298309,37.27365664691258;126.12030051466729,37.24683567659007;126.16501994179943,37.231520452069546;126.11931236583665,37.21159807869733;126.08963928755354,37.24726670706811;126.10489742298309,37.27365664691258;126.43921536373273,37.23113445861797;126.43502765150147,37.27291676577788;126.47139614227595,37.28452211198935;126.49649461384448,37.2558167689752;126.45781951688586,37.226038140761055;126.43921536373273,37.23113445861797;126.41078726302332,37.411039259746744;126.44129435680796,37.38491394270893;126.41386011569953,37.366484488831475;126.41078726302332,37.411039259746744;126.32141007123677,37.7521179310233;126.3199918547476,37.71163641955494;126.36312215612392,37.69571544695772;126.3698741458859,37.66341975854123;126.33871873184843,37.64743949314964;126.3156769559878,37.684576686919556;126.28246175095768,37.702919090249736;126.28981100240654,37.74072226751897;126.32141007123677,37.7521179310233;126.26451707478925,37.81778583885214;126.29773328966907,37.802199043695516;126.31577075890729,37.77398861988196;126.29081580750011,37.7629425837148;126.24827008507194,37.76560034570511;126.21634667621532,37.77815916115239;126.22322045538863,37.80503670281207;126.26451707478925,37.81778583885214;126.43121685572747,37.82986908194544;126.5068975008081,37.78234762310096;126.52648366049696,37.747326128313986;126.51372172326727,37.72503438286504;126.52251279524307,37.651887425219904;126.54274511634391,37.6178090830475;126.51062866491168,37.59662373953827;126.40316813158975,37.59428252797514;126.3792856257021,37.60958611335555;126.3767633555897,37.636293098460364;126.41263172449081,37.656405203316694;126.39236685339195,37.69419397340533;126.3555906986772,37.70667699662161;126.35057985274231,37.789565062650894;126.38817572692581,37.80670536628266;126.3949295459992,37.82287154699949;126.43121685572747,37.82986908194544;124.70691404651816,37.84705148177522;124.71788130307019,37.81383758257668;124.6799885505802,37.817024381490505;124.70691404651816,37.84705148177522;124.6871734927767,37.98047304204017;124.729720445775,37.97811533012657;124.69626248901929,37.91707230639538;124.63738695819157,37.923945353998434;124.62302020632995,37.95696971255016;124.6871734927767,37.98047304204017";

//성남
//        tmp = "127.132852896438536,37.474627355612611;127.153600397015254,37.473587405762899;127.153734407873785,37.473555337739697;127.153758033175805,37.473554406302618;127.154214600360532,37.473536337767285;127.154600291849079,37.47352124251308;127.154488870174305,37.471767856228666;127.163002891796893,37.47153785485331;127.166736699465062,37.469662347561552;127.178805604604165,37.474820510482218;127.180470282617932,37.474008072786212;127.180947983383717,37.473459151885812;127.182020767094571,37.46639192978737;127.189388755441456,37.45835274007662;127.193755172320607,37.434785687862664;127.195429905723273,37.430944929341884;127.194328676659765,37.424622028336763;127.185561911918413,37.418533876983865;127.177592407902424,37.410680739036856;127.176852475413739,37.409031338487097;127.177260903437741,37.387089514351324;127.16180058795247,37.379644228566796;127.150535325750056,37.361503456324662;127.134251603749547,37.356522801668625;127.13794974548766,37.339671059385878;127.122760011845884,37.336800001292218;127.121952509645936,37.336188940986446;127.120829045044673,37.334189067853458;127.116413701765723,37.333917463291904;127.114788134041987,37.335116040830862;127.113719943299017,37.335697678313025;127.11307679192484,37.335806048912644;127.112496311843543,37.335792088339666;127.111598733298393,37.335460178809242;127.111031156196972,37.335082455756755;127.110450426068454,37.334422361189468;127.109144880854288,37.335776353084967;127.108290733460933,37.335903180200539;127.107703231152286,37.33643179887784;127.105814924601873,37.337580301261092;127.104637204411674,37.338104918293936;127.104379394988726,37.338384369846352;127.103964532490053,37.338437349565382;127.102323295514523,37.340477135018766;127.102065159157277,37.342750067639535;127.101820504292675,37.343138859343696;127.098030372047674,37.34285038699084;127.094843489975105,37.345364580123551;127.093214974066058,37.345848486176678;127.092978630320502,37.346994264739763;127.088828156286326,37.348523822052222;127.087348196496194,37.349722759456718;127.08662836201789,37.351246970083203;127.082666098826365,37.351607793337294;127.081478548398081,37.350788582349281;127.076236578057575,37.350571214094444;127.07058886630648,37.357975246186157;127.070620476223993,37.358005150615142;127.063190564812444,37.360452527621646;127.061067423973185,37.360695359269847;127.05899064702173,37.360784028817555;127.05603785297896,37.360094416299717;127.027712458343117,37.372848459020346;127.038743803147895,37.391941670772482;127.037110553174216,37.401306058972956;127.042294067502283,37.428618892986627;127.069683022164952,37.430607423620742;127.070005102387356,37.430937970953998;127.07090618783873,37.430213989008429;127.07118075835028,37.430504105716757;127.071231313063208,37.430863515569946;127.07158901217889,37.441554634786407;127.075341099269039,37.441946830086152;127.076144440287493,37.442132590914682;127.076844013169222,37.44192127535014;127.083954229930612,37.44390795488863;127.093646220341071,37.45620420033228;127.09507222734868,37.456421600932678;127.099038547518546,37.456284622432818;127.102639011102539,37.459867064773199;127.104777854817371,37.461165563137406;127.104345088942338,37.462159036515395;127.107331971635318,37.462174050578625;127.108163803167244,37.462267800184648;127.117464669827669,37.462201025376622;127.125063615537869,37.467335813586018;127.124877993804873,37.469606256191824;127.125201779771771,37.469632082874007;127.126832700616177,37.468585930888374;127.130687899040851,37.467727394549371;127.132799600336497,37.468393701940421;127.131982776458187,37.469798279147504;127.132824236013235,37.471200965349801;127.132842620812113,37.471532603999179;127.132626551839067,37.472351945363414;127.132736104214501,37.474556306191147;127.132852896438536,37.474627355612611";

//파주.
//        tmp = "126.972224606299164,38.007524867714665;126.989397741291825,38.008870265683171;126.993150884116375,38.004079945258162;127.016064807909203,37.991968980811649;127.017107137458254,37.986962813038225;126.957257256506296,37.921553995356561;126.956598399090481,37.921782078050036;126.93940399347585,37.852632543048621;126.925100025758553,37.841587341915172;126.925062330267735,37.841608075329738;126.924971977871948,37.841588403379781;126.913312339865456,37.840790982472981;126.911990751076658,37.791467199927169;126.918663834554451,37.790203150968416;126.935595612921233,37.776396282159638;126.935605329515425,37.776396855672843;126.935602725275345,37.776379113760562;126.920218959508759,37.747027828237385;126.888533990503859,37.722964892451365;126.856032916333461,37.735586384967831;126.855996808680089,37.735561563743943;126.833938547085552,37.725231172749389;126.814287796510797,37.723434588571031;126.804015558819643,37.727269443553858;126.798453009655844,37.737044156788002;126.794021309432267,37.734425336450506;126.793835018887279,37.72613547282667;126.793834231016817,37.726134754868852;126.773417452481823,37.703871366846187;126.726450301428386,37.70328351430642;126.689916052325316,37.688605726821827;126.673951337620082,37.700299250708817;126.662623072104097,37.780699844141623;126.662617860761273,37.780724710348267;126.67123697970996,37.834679320595768;126.659393017629739,37.839915738927388;126.66043523717093,37.84322240888357;126.6757493714139,37.844914491206971;126.670136299563552,37.888025908147711;126.67739862562243,37.898064240827615;126.675469267283319,37.906558089270646;126.68219014611482,37.912095849971614;126.681967368303518,37.914422749953303;126.672918075335346,37.913674546129585;126.665836667374876,37.919956861806604;126.670990840888734,37.955177726872137;126.707696605248728,37.975643318036774;126.796393866094135,37.993695038477142;126.796397126207722,37.99369236039194;126.820544894737878,37.957668141992954;126.835788263099559,37.960194357201978;126.835787836562503,37.960194587537273;126.835863299569752,37.960316914220648;126.835940543708389,37.960442586311672;126.840127284076544,37.97898691896777;126.848809575755041,37.98653924057961;126.886134313089201,37.966148720907277;126.886227064926047,37.9661379135799;126.88630344283709,37.966147798323142;126.894053712046968,37.969483822701172;126.893597603437271,37.993673776859929;126.937469023517565,37.980988749503034;126.972217443236602,38.007528802387775;126.972224606299164,38.007524867714665";

//천안.
//        tmp = "127.290409836814632,36.892396770294198;127.307997280983216,36.885064044184489;127.359180709406971,36.8220044451338;127.381490313197233,36.814082702248847;127.394982769016465,36.784094754656898;127.403318157555034,36.774474973431765;127.413077577146296,36.773058934178316;127.420317776521856,36.761012090331612;127.419890706981647,36.757876899031913;127.412802166014416,36.757420929606432;127.410924230464516,36.756057155312334;127.404198090010155,36.744850346739291;127.385593237192779,36.759539635385295;127.33550795250261,36.752641846202053;127.343166205836155,36.735695292059887;127.33920284119796,36.729725185974836;127.329398567366312,36.734454155048553;127.318461419924063,36.725250195132496;127.315561985217656,36.724185556588772;127.28526315057654,36.690606043445833;127.215110192203696,36.718213314307746;127.211449178722518,36.718292758576148;127.171462954971801,36.732037204270981;127.167840601591394,36.733090436215079;127.157525637580747,36.727456095036842;127.151934287731592,36.728952109654685;127.152051284880784,36.726807584420683;127.151945710919279,36.726681838568467;127.149288425509042,36.725932179155357;127.14762072455639,36.722303014605529;127.148819334980914,36.721018013290369;127.135669738311776,36.713018959350308;127.141748934128898,36.689983826352929;127.156573499188553,36.691020883089656;127.157093061109151,36.695080035169624;127.158054108377272,36.695324611148763;127.161444902245819,36.690218629817267;127.159938692488495,36.68840938873285;127.160769621895241,36.687701742019406;127.163932777606689,36.679507954169779;127.160049896192675,36.626369480203934;127.150901768011678,36.620151374499088;127.127632054181007,36.653353396483141;127.123959732558362,36.652992831104598;127.111415114645567,36.658807770778182;127.0447713316358,36.651302277562131;127.039920450404196,36.65470315755411;127.020408081366682,36.650142231830145;127.012849978621546,36.654414552512073;127.072939325622471,36.708366194889273;127.086530469580651,36.749263452503271;127.096113398996749,36.751443950694508;127.098163917741033,36.755725689441618;127.092075145226417,36.761032965755554;127.116526073381053,36.762879671930044;127.11758579009836,36.78046408078896;127.11271561701038,36.782552546742878;127.100644887604474,36.782709861146706;127.100840742070446,36.788465262658882;127.095586797780854,36.795912725899328;127.096926680676674,36.815692314591232;127.102151023801042,36.82520253329929;127.100602858669248,36.827273825899049;127.105617643244472,36.836486105937738;127.105322247800146,36.837862237526828;127.098172089837902,36.837957527991755;127.0981690681472,36.840153682272287;127.110499886598774,36.854108068291573;127.106839591262982,36.860292819862977;127.087420302377296,36.861862392622207;127.088342165384461,36.889309709514841;127.092951752649995,36.893611201130476;127.091915427668013,36.89424556014832;127.089424788249005,36.899838264176609;127.0890187772156,36.900267257829398;127.087344603991923,36.907225953844708;127.094029468795497,36.911484642097101;127.080365675823018,36.930252085121118;127.079667718966363,36.932580600205419;127.074983887323,36.935702454315638;127.074902309431664,36.939856161555269;127.085661859316318,36.947532459472768;127.095276058198323,36.942334932548498;127.097003204522167,36.947994128433521;127.102918317541764,36.95327829726039;127.096680490983985,36.95556658524815;127.09653325315189,36.959127256172806;127.107899584689676,36.968809703074143;127.110853220394958,36.969224343767337;127.114779767282002,36.971156285530121;127.120382291821244,36.970229738289632;127.136780969034447,36.965661729865452;127.158877323422914,36.969923618916646;127.16355628227214,36.962654649438129;127.173427079422467,36.962793946077568;127.179534420125108,36.957369454205256;127.179657179485616,36.957288173651662;127.20214345268171,36.951710662199069;127.202109627730366,36.95165665460425;127.220035973610592,36.930467596211521;127.235516775129625,36.924031399282349;127.244049169885514,36.917598612407964;127.248934152768612,36.919039232334242;127.258608526801694,36.915873900497957;127.265036666030369,36.915229053463833;127.274288801324602,36.913442042615173;127.289875839841756,36.894022071880983;127.290409836814632,36.892396770294198";

//대전.
//        tmp = "127.291875706480411,36.263546280192053;127.291751123763063,36.264687698143774;127.290136573443633,36.265911421100668;127.282092622708944,36.264927598661224;127.281245251582163,36.265184889105271;127.279495868500618,36.266033584929893;127.258317618753054,36.276373320326314;127.254219814366238,36.280474195611227;127.253638125619986,36.282362364446854;127.251262032655902,36.283990010598259;127.246652951859986,36.290959041523109;127.258417709150265,36.303503207457219;127.258642244236526,36.309208041440847;127.25801224853241,36.309933013875394;127.259766482591985,36.327254305491394;127.26618666961015,36.328391782202765;127.268120954252169,36.331409113279712;127.270569403460698,36.360857245028875;127.272341036101963,36.362618219594061;127.282120352802067,36.414610498512623;127.291086114315235,36.41615151736827;127.293485428276867,36.418576250147453;127.295312063365742,36.419769736601353;127.294192033619311,36.422202538424258;127.296418241975076,36.423207937070444;127.341061314977466,36.430813363081278;127.356712097235174,36.458944416492081;127.357667812320102,36.46148102614243;127.356069066767674,36.464014374952264;127.35900346259055,36.467610022034819;127.363853489293689,36.474362192546181;127.363884141936424,36.475220580424399;127.357762718798128,36.483185108307438;127.36098768736008,36.490877184599178;127.368905547541431,36.489589738764479;127.380336849467398,36.499538917426158;127.383855394986227,36.500230190735124;127.403014362841461,36.485483169923235;127.400903319135153,36.457566055858756;127.402550216483732,36.455561733574491;127.414968799281326,36.454706397791917;127.42745213188158,36.456871280321735;127.436769111688577,36.456402070526131;127.44716884398963,36.449203454430162;127.449631477224273,36.449231713908702;127.454762853644553,36.450274779513244;127.459989180744245,36.453650978020079;127.466695704890142,36.470072593339488;127.47093407261498,36.474142348878914;127.472428128799066,36.475020064101066;127.476130865248066,36.47633102692911;127.48403346868372,36.476621891574325;127.477481730525113,36.458937128357704;127.484544653533874,36.452928836522844;127.500591927932732,36.456202675358043;127.504455229706849,36.451896487198098;127.504473059802834,36.451831583413494;127.489721719471319,36.434124700123455;127.501554863221514,36.408653359366753;127.501562432876057,36.408653327670748;127.509497263637144,36.408619838787274;127.513924057575167,36.420439317404629;127.542280682680527,36.419278821222576;127.559678961509235,36.398218349080935;127.533674110398934,36.392939609960429;127.533423832836363,36.391747079604926;127.52940215159245,36.389356921381768;127.526578222665307,36.370067156608378;127.522395531432281,36.35432430910457;127.521607421055393,36.353746410776886;127.519369035617004,36.350352456339607;127.517112865865087,36.349063608397437;127.517625200256731,36.348146084421401;127.517282560399892,36.348012821730322;127.515776266025284,36.348677866296711;127.507436673332919,36.346293856128753;127.507459719524206,36.346286702436942;127.496228897586931,36.278755014695093;127.488753469757484,36.272710129154717;127.483545891114133,36.230356403347095;127.477857101948487,36.229668206763982;127.455591662037605,36.200800209821665;127.443694517619079,36.194752739310978;127.441101927079089,36.194786359594325;127.412553246065755,36.211072047712861;127.409975874423253,36.211703539765921;127.409537111296018,36.212168202486303;127.400968707151549,36.230979345957124;127.387337530007855,36.234150771563257;127.389609943670195,36.263019254828308;127.377613723451148,36.271468917520707;127.377592141160179,36.271468282469272;127.372460484925512,36.272183030984927;127.372447803311545,36.272160693998693;127.364533484265962,36.219846377964465;127.364524972656312,36.219804705770933;127.331717136155049,36.184045633659103;127.318090981049465,36.216007551250947;127.305239827341325,36.227887772145721;127.303454348649723,36.227168611567322;127.303563099673909,36.221827144698864;127.30159876269154,36.22152244617903;127.295929128599752,36.222195598318898;127.294596704518327,36.22298068814888;127.293570743421853,36.223120348918904;127.293332117308765,36.22417162896329;127.29344317516879,36.224336465123812;127.291662544929125,36.226507581753452;127.291635683718297,36.226793693587481;127.287351217731086,36.234396634916585;127.286106981178946,36.234414389630452;127.282945775698622,36.235401297724785;127.28288997683218,36.235661773404097;127.281930062406317,36.235692639922959;127.28180975447971,36.235935233490373;127.282435791213928,36.23631068730684;127.281127574028318,36.245911890834506;127.280389843431053,36.249293271598859;127.28031477766713,36.250189253205463;127.279917174759646,36.250769020425594;127.27957872064971,36.252324309250284;127.279107618993123,36.25259486719056;127.279144975430853,36.253108898243923;127.281914349234711,36.255080469569748;127.282140998671764,36.257254255796049;127.282358259842042,36.25816643945744;127.284772236699965,36.259983862686639;127.291875706480411,36.263546280192053";

//수원
//        tmp = "126.933219371580464,37.303114930098936;126.943172681693852,37.300749030524756;126.950995391164398,37.300333130914659;126.954406275625502,37.302934755928611;126.961809080172898,37.302643798368955;126.965377180710519,37.306922744042964;126.965950240994019,37.316862604155929;126.96836725195476,37.318645645450303;126.969099386557488,37.320533182379336;126.968513289298457,37.327014794272969;126.9713139435638,37.33174501339532;126.982912425100224,37.326671894708646;126.984708235524096,37.328449976391212;126.986556637439605,37.329347239982475;127.017445323257519,37.352493801862387;127.033597015188178,37.344546810497853;127.037744877569509,37.325386847948458;127.045977038138261,37.317246035090143;127.05681644797491,37.315649855859903;127.060630190132926,37.310980050459278;127.061041055375426,37.30228876756258;127.060389268842869,37.296998662821053;127.079276887716617,37.293543458118769;127.079488697164379,37.29528381576273;127.082832687404363,37.29763049925441;127.089623609761389,37.298268937860023;127.087967783321517,37.290952568820082;127.08933108132662,37.28690107048336;127.075197405701914,37.282337708934065;127.068790527285515,37.278504108898439;127.069340283178548,37.277056862552904;127.066424531947376,37.273713828278794;127.06402405522681,37.269366553140856;127.069135762683118,37.268627060734929;127.072137924819359,37.266360512479807;127.077334963560617,37.265689521445907;127.077188726927943,37.26679993284931;127.077179104761029,37.266873283204077;127.081270020865475,37.267591824390692;127.084880290007249,37.26991757093775;127.084892330323541,37.269896478264414;127.086225990048675,37.264877761466025;127.080771888209313,37.259892326605112;127.080949258588689,37.258467427160127;127.083625121411131,37.258750941677768;127.08464308566262,37.256427099669864;127.082058241247623,37.254211832600348;127.082391626233402,37.249451996606638;127.080094142705761,37.247388024073679;127.071043492537939,37.24647111797703;127.068405728538991,37.24475312698943;127.065440810658089,37.243727269880388;127.066893369088518,37.240098637197725;127.063141312845445,37.23848600152472;127.052933682164308,37.231528934285038;127.051466920316358,37.234297633636196;127.046168410228404,37.233135977471669;127.046222552770487,37.235904008515625;127.04354792380731,37.236869646176679;127.043601296956481,37.238463352707747;127.037254283594663,37.238884921024628;127.034682792991717,37.236453172573292;127.030223065498006,37.233812163225821;127.02922554920228,37.231463762397979;127.02329820489426,37.230416571455748;127.024267660108464,37.227293508797878;127.021355916937353,37.22471306544378;127.002370319855103,37.229894586615686;126.997859678234477,37.226497432133201;126.99787773433836,37.228964493185686;126.993270152173125,37.230083150118006;126.987473962092068,37.232515211135293;126.984658718419354,37.230803139505724;126.97145108099852,37.229115275307947;126.9696148804519,37.23024720441331;126.967908251615171,37.234923086559178;126.961507552487717,37.237521469867154;126.957654834341042,37.23990781827613;126.957982952723142,37.242934590597997;126.956064287463818,37.246802962801254;126.952904302108266,37.248884134197581;126.949913165393014,37.255800224593578;126.946776819749715,37.257445613093594;126.935826393792027,37.256853723093137;126.92764620764757,37.277416148093543;126.93256238529743,37.292072419013074;126.9394102696413,37.29522160464554;126.939225269325078,37.296794250234406;126.934827204271272,37.298479984424091;126.933219371580464,37.303114930098936";

//대구
//        tmp = "128.475102671301016,35.895225673059237;128.504627609573419,35.892841004189748;128.507648859573436,35.908827382629262;128.528849690598065,35.922380387018897;128.533673928451861,35.933461421616649;128.534875983341863,35.958403387702361;128.530175040559413,35.980262465102768;128.544734095447012,35.970744521790792;128.548058568798638,35.962817140864892;128.553407484508739,35.960740997054536;128.553417123852967,35.960800729978011;128.554603481116402,35.963911047466958;128.556436844779199,35.966672896445417;128.556813464352444,35.972224339073911;128.579393860418634,35.977871449607811;128.605591955101289,35.991069169346673;128.610689374179913,35.998446410413486;128.69499994354706,36.016386447265056;128.743650262229892,35.966210072441292;128.742868180046088,35.963874570819613;128.742961177653171,35.963408780609917;128.742864743553952,35.963099288791646;128.737529488678007,35.956227805144152;128.737967005875248,35.933334191676373;128.757404004546743,35.876400866903744;128.759013432410057,35.87628340461049;128.761106041708757,35.875013889423386;128.76112368756975,35.874561165909697;128.757350103092762,35.870003013171285;128.759799903577687,35.867690473698858;128.759806933522583,35.866498118016466;128.73856248266938,35.851432744336606;128.724360863813928,35.858629058357565;128.726370022259374,35.842959060400048;128.7250138180367,35.835993419724851;128.718557450740207,35.83556364371168;128.708785678398584,35.826804882112711;128.713276706421908,35.805522702256589;128.691115688654037,35.761001556429896;128.691118710333598,35.760992253870974;128.683237728150431,35.721041082530284;128.663221745449903,35.719756697375388;128.623697760897329,35.701894077737371;128.561283848665852,35.730045867538401;128.534050063974661,35.718021154828627;128.530176873061237,35.682397119722502;128.506636256425054,35.674037235164576;128.499624574630985,35.640668018909906;128.471658730924673,35.636646819625781;128.461226336319442,35.640363241793359;128.453084033978371,35.636497350751938;128.447201243103819,35.637800367531696;128.430769845458599,35.621683610000709;128.423422587877241,35.625467224519952;128.411319966603031,35.616341409373405;128.402806526866243,35.617244453685309;128.381693091093183,35.6065594005889;128.372430718059462,35.609665409935758;128.400032207170995,35.629344190818877;128.399625998980753,35.643049583406068;128.37343091757117,35.662383952357963;128.346296161346146,35.696227469346027;128.361985687837802,35.707408472687128;128.419657831582612,35.693562439023111;128.425598014053321,35.697827452731708;128.434987962323447,35.707367645335637;128.434815452070865,35.719806971604328;128.388266366181739,35.751366311770468;128.385758078801388,35.754688825447857;128.386279667946155,35.764681636097862;128.421348853906323,35.807237099028427;128.472568481160749,35.810156228409475;128.476060762928142,35.815082770753122;128.473118036642347,35.829220929074744;128.448896828926991,35.846604718043764;128.393928224239261,35.852532662422433;128.383196935064689,35.871218520965193;128.39369721152508,35.884490027860174;128.416465827235697,35.912972242276787;128.43838541396147,35.931212693629078;128.459665408944801,35.942205611038638;128.474849916193477,35.936877203767018;128.465757748232079,35.901096238145968;128.475102671301016,35.895225673059237";

//평택
//        tmp = "126.910808443573899,36.901559768823155;126.842705163831596,36.905309281813217;126.830826779315146,36.944963485621713;126.828613473453586,36.945786793635783;126.828163321459485,36.946428888741345;126.829183350151723,36.947560880050702;126.844761997646941,36.95435551135806;126.825342842247622,36.981625655313685;126.78921883983449,36.994982406358773;126.779010617318548,37.007241919664139;126.787904963517121,37.013267998882327;126.833996373109031,37.013391153601084;126.853835813583061,37.019777455182712;126.879848079936409,37.056698921392368;126.883718537723297,37.06699239134457;126.883718311683509,37.067000410596151;126.897750090852071,37.068478989731105;126.903465027410689,37.072866781660032;126.911348337573287,37.068892042563022;126.914764179965331,37.065677660982772;126.928460415127887,37.068179798155924;126.931476100894926,37.062722924086444;126.944001085428084,37.060899481730971;126.969354697125326,37.073424849223798;126.996186084256252,37.069686477051206;126.999363752181125,37.1247177611803;127.02115995716774,37.132114636356675;127.03573196014699,37.136459337209708;127.038199559158627,37.135799694855756;127.038398586714905,37.132100253262649;127.045624558124942,37.125824281493877;127.045631691130168,37.125841299790807;127.045201953099081,37.129263003930625;127.052006984787312,37.129602004435817;127.052450929441918,37.131091837924529;127.055475035594071,37.133520969317033;127.058261486974118,37.132333419111191;127.064917652784516,37.131962939369849;127.064843177290911,37.13119638840999;127.066278785707482,37.131244777242571;127.066852721751033,37.127142968665197;127.069841978286931,37.128508407688379;127.071044593932015,37.125672841316273;127.074032340321253,37.125469959851998;127.079113072363583,37.128127452909055;127.079120405886556,37.128134917860066;127.079104692931594,37.125848685207067;127.079751540172779,37.12612972651192;127.08591040744561,37.126929636658616;127.090336113218271,37.129748631856117;127.091014306484524,37.131013300020093;127.091292301287098,37.131416640680669;127.087304905673264,37.135943338409433;127.097672546308999,37.136788775454519;127.116086764582121,37.144272714122827;127.120162094240072,37.130718664113438;127.108460587686068,37.126225056273825;127.109555122585803,37.119106759185684;127.109529527481257,37.117631167655702;127.11251862980302,37.113544255937292;127.126517036808693,37.082819710338903;127.12171166339516,37.076646697627908;127.128139243352607,37.062591828855993;127.132997663888219,37.05764051636892;127.120943636319751,37.046850379606397;127.1095931870247,37.040936595407281;127.121923421610745,37.03497330043934;127.141178443693903,37.031976318961654;127.145212576230719,37.017012834783898;127.153798599415509,36.997620101843616;127.145074301264657,36.997027912298599;127.14055791929556,36.99462065434728;127.134589482216427,36.993019109613599;127.136476176984829,36.982803212725116;127.141121820454273,36.979741986856361;127.141126311409536,36.979003422028249;127.138061763054296,36.979574396038672;127.137307921399938,36.97844934419944;127.133892636923321,36.976858662943165;127.133474081810604,36.976859408243556;127.117767890611063,36.970799956065939;127.114811557558994,36.971124545231824;127.111000833808106,36.969801182349549;127.098259246653555,36.960161588754687;127.096199028183349,36.955579129819697;127.103060133171937,36.953080240229802;127.094878876715171,36.941546249568866;127.086419305521559,36.947504067369699;127.074081046425889,36.938628794023018;127.073011118924057,36.940857996944075;127.069729117887192,36.940075836489804;127.060761661012577,36.939618325415495;127.058956701213972,36.937628743764428;127.055888289703091,36.93740906148517;127.052831163106134,36.935369703566664;127.047920810869314,36.933300869354227;127.045616112836825,36.933378430120541;127.041233151332293,36.932368450478585;127.037943446573706,36.93259074378723;127.035227087947973,36.930636081507416;127.034858006376027,36.929877348414905;127.031020433514584,36.928102977344167;127.017215133029239,36.933952680371377;126.995122955927016,36.935313513585065;126.993118394729436,36.934567146145071;126.991538688664761,36.933508726189736;126.910808443573899,36.901559768823155";

//제주
        tmp = ";";

        Savepoint_suburb(tmp);

        Readpoint_suburb();

//    for (int i = 0; i < msuburbpoint.size(); i++) {

//            Log.d(TAG, "x " + msuburbpoint.get(i).x + " y " + msuburbpoint.get(i).y);

//        }

    }

/////////////////////
    public static boolean Suburb_check(double x, double y)
    {

        String sResult = "";
        boolean bResult = false;
        int hitCount = 0;

            double maxX = 0;
            double maxY = 0;
            double minX = 600000;
            double minY = 600000;

            int idx = 0;

            for (idx = 0; idx < msuburbpoint.size(); idx++) {
                gpspoint pxy = msuburbpoint.get(idx);
                maxX = __max(maxX, pxy.x);
                maxY = __max(maxY, pxy.y);
                minX = __min(minX, pxy.x);
                minY = __min(minY, pxy.y);
            }
            if (maxX < x || x < minX) {
                return false;
            }
            if (maxY < y || y < minY) {
                return false;
            }

            /* 다각형의 선분과 만나는 지점의 갯수 */
            /* 클릭한 좌표가 검사하는 선분의 Y좌표 범위를 벗어난다면 검사하지 않음 */

            ArrayList<gpspoint> tPoint = new ArrayList();

            for (idx = 0; idx < msuburbpoint.size(); idx++) {

                if (idx != (msuburbpoint.size() - 1)) {
                    tPoint.add(msuburbpoint.get(idx + 1));

                } else {
                    tPoint.add(msuburbpoint.get(0));
                }
            }

            /* 다각형내부 좌표인지 검사 */
            gpspoint tmpPt = new gpspoint(x, y);
            gpspoint tmpPt2 = new gpspoint(x + maxX, y);

//			util.log("waitarea[i].gpspoint.length : "
///					+ waitera.get(i).gpspoint.size());

            for (idx = 0; idx < msuburbpoint.size(); idx++) {
                /* 클릭한 좌표가 검사하는 선분의 Y좌표 범위를 벗어난다면 검사하지 않음 */
                gpspoint p = msuburbpoint.get(idx);

                if (__min(p.y, tPoint.get(idx).y) > y) // 해당선분의 최소Y값보다 작을때
                    continue;
                if (__max(p.y, tPoint.get(idx).y) < y) // 해당선분의 최대Y값보다 클때
                    continue;

                /* 교차여부 검사 */
                // 대기지역좌표1, 대기지역좌표2/ 현재좌표/ 최소좌표(가상점)
                if (Intersection(msuburbpoint.get(idx),
                        tPoint.get(idx), tmpPt, tmpPt2)) {
                    hitCount++;
                }
            }

            /* 만나는지점의 갯수가 짝수(0,2,4,6...)라면 외부 홀수(1,3,5,7...)라면 내부 */

            if (hitCount % 2 == 0) {

                bResult = false;

            } else {

                bResult = true;

            }

        return bResult;

    }

    static boolean Intersection(gpspoint p1, gpspoint p2, gpspoint p3, gpspoint p4) {
        if ((SignedArea(p1, p2, p3) * SignedArea(p1, p2, p4) <= 0)
                && (SignedArea(p3, p4, p1) * SignedArea(p3, p4, p2) <= 0)) {
            return true;
        } else {
            return false;
        }

    }

    static double __min(double x, double y) {
        if (x < y) {
            return x;
        } else {
            return y;
        }
    }

   static double __max(double x, double y) {
        if (x > y) {
            return x;
        } else {
            return y;
        }
    }

   static int SignedArea(gpspoint p1, gpspoint p2, gpspoint p3) {
        double area = ((p1.x * p2.y - p1.y * p2.x)
                + (p2.x * p3.y - p2.y * p3.x) + (p3.x * p1.y - p3.y * p1.x));

        if (area > 0)
            return 1;
        else
            return -1;

    }

//////////////

    public static void _get_SuburbVersion() {

        SharedPreferences editor = Info.gMainactivity.getSharedPreferences("last_login_info", MODE_PRIVATE);
        try {
            Info.APP_SUBURBSVER = Double.parseDouble(editor.getString("suburbs", "0.0"));
        }
        catch (Exception e) {
            ;
        }

        if (Info.APP_SUBURBSVER < Info.SV_SUBURBSVER) {

            Log.d("Suburbs", "download a " + Info.APP_SUBURBSVER + " s " + Info.SV_SUBURBSVER);
            _downloadSuburbs();
        }

        Readpoint_suburb();

    }

    private static void _set_SuburbVersion(double dvalue) {
        SharedPreferences sf = Info.gMainactivity.getSharedPreferences("last_login_info", MODE_PRIVATE);
        SharedPreferences.Editor editor = sf.edit();

        editor.putString("suburbs", dvalue + "");
        editor.commit();
    }

    private static void _downloadSuburbs()
    {
        try

        {

            String downsuburbs = new ConnectionHtml().execute(setting.FILESERVERSUBURB + Info.AREA_CODE + "suburb.txt", "file", "appmeter", "suburb.txt").get();
            if(downsuburbs.equals("false") == false)
            {

                _set_SuburbVersion(Info.SV_SUBURBSVER);
                Info.APP_SUBURBSVER = Info.SV_SUBURBSVER;

            }

        } catch(
                NullPointerException e)

        {

        } catch(
                InterruptedException e)

        {
            e.printStackTrace();
        } catch(
                ExecutionException e)

        {
            e.printStackTrace();
        }
    }
}
