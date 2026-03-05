package com.github.tommyettinger.digital;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * A variant on Hasher that uses the a5hash32 algorithm, which is meant for small key sizes.
 * Uses the same functional interfaces as Hasher.
 * In benchmarks, this is almost always significantly slower than the existing hash() methods in Hasher, and often the
 * hashBulk() methods as well, even for small keys. It also is limited to producing 32-bit output. The optimizations
 * that make this so quick for small keys in C/C++ aren't available in Java. It's always slower on large keys than
 * either existing algorithm in Hasher.
 */
public final class HasherA5 {

    /**
     * Alternating 0, 1 bits.
     */
    public static final int VAL01 = 0x55555555;
    /**
     * Alternating 1, 0 bits.
     */
    public static final int VAL10 = 0xAAAAAAAA;

    /**
     * The seed used by all non-static hashA5() methods in this class (the methods that don't take a seed).
     * You can create many different HasherA5 objects, all with different seeds, and get very different hashes as a result
     * of any calls on them. Because making this field hidden in some way doesn't meaningfully contribute to security,
     * and only makes it harder to use this class, {@code seed} is public (and final, so it can't be accidentally
     * modified, but still can if needed via reflection).
     */
    public final long seed;

    /**
     * Creates a new HasherA5 seeded, arbitrarily, with the constant 0xC4CEB9FE1A85EC53L, or -4265267296055464877L .
     */
    public HasherA5() {
        this(0xC4CEB9FE1A85EC53L);
    }

    /**
     * Initializes this HasherA5 with the given seed, verbatim; it is recommended to use {@link Hasher#randomize3(long)}
     * on the seed if you don't know if it is adequately-random. If the seed is the same for two different HasherA5
     * instances, and they are given the same inputs, they will produce the same results. If the seed is even slightly
     * different, the results of the two HasherA5s given the same input should be significantly different.
     *
     * @param seed a long that will be used to change the output of hashA5() methods on the new HasherA5
     */
    public HasherA5(long seed) {
        this.seed = seed;
    }

    /**
     * Constructs a Hasher by hashing {@code seed} with {@link Hasher#hash64(long, CharSequence)}, and then running the result
     * through {@link Hasher#randomize3(long)}. This is the same as calling the constructor {@link #HasherA5(long)} and passing
     * it {@code randomize3(hash64(1L, seed))} .
     *
     * @param seed a CharSequence, such as a String, that will be used to seed the Hasher.
     */
    public HasherA5(final CharSequence seed) {
        this(Hasher.randomize3(Hasher.hash64(1L, seed)));
    }
//<editor-fold defaultstate="collapsed" desc="Predefined Instances">

    public static final HasherA5 alpha = new HasherA5("alpha"), beta = new HasherA5("beta"), gamma = new HasherA5("gamma"),
            delta = new HasherA5("delta"), epsilon = new HasherA5("epsilon"), zeta = new HasherA5("zeta"),
            eta = new HasherA5("eta"), theta = new HasherA5("theta"), iota = new HasherA5("iota"),
            kappa = new HasherA5("kappa"), lambda = new HasherA5("lambda"), mu = new HasherA5("mu"),
            nu = new HasherA5("nu"), xi = new HasherA5("xi"), omicron = new HasherA5("omicron"), pi = new HasherA5("pi"),
            rho = new HasherA5("rho"), sigma = new HasherA5("sigma"), tau = new HasherA5("tau"),
            upsilon = new HasherA5("upsilon"), phi = new HasherA5("phi"), chi = new HasherA5("chi"), psi = new HasherA5("psi"),
            omega = new HasherA5("omega"),
            alpha_ = new HasherA5("ALPHA"), beta_ = new HasherA5("BETA"), gamma_ = new HasherA5("GAMMA"),
            delta_ = new HasherA5("DELTA"), epsilon_ = new HasherA5("EPSILON"), zeta_ = new HasherA5("ZETA"),
            eta_ = new HasherA5("ETA"), theta_ = new HasherA5("THETA"), iota_ = new HasherA5("IOTA"),
            kappa_ = new HasherA5("KAPPA"), lambda_ = new HasherA5("LAMBDA"), mu_ = new HasherA5("MU"),
            nu_ = new HasherA5("NU"), xi_ = new HasherA5("XI"), omicron_ = new HasherA5("OMICRON"), pi_ = new HasherA5("PI"),
            rho_ = new HasherA5("RHO"), sigma_ = new HasherA5("SIGMA"), tau_ = new HasherA5("TAU"),
            upsilon_ = new HasherA5("UPSILON"), phi_ = new HasherA5("PHI"), chi_ = new HasherA5("CHI"), psi_ = new HasherA5("PSI"),
            omega_ = new HasherA5("OMEGA"),
            baal = new HasherA5("baal"), agares = new HasherA5("agares"), vassago = new HasherA5("vassago"), samigina = new HasherA5("samigina"),
            marbas = new HasherA5("marbas"), valefor = new HasherA5("valefor"), amon = new HasherA5("amon"), barbatos = new HasherA5("barbatos"),
            paimon = new HasherA5("paimon"), buer = new HasherA5("buer"), gusion = new HasherA5("gusion"), sitri = new HasherA5("sitri"),
            beleth = new HasherA5("beleth"), leraje = new HasherA5("leraje"), eligos = new HasherA5("eligos"), zepar = new HasherA5("zepar"),
            botis = new HasherA5("botis"), bathin = new HasherA5("bathin"), sallos = new HasherA5("sallos"), purson = new HasherA5("purson"),
            marax = new HasherA5("marax"), ipos = new HasherA5("ipos"), aim = new HasherA5("aim"), naberius = new HasherA5("naberius"),
            glasya_labolas = new HasherA5("glasya_labolas"), bune = new HasherA5("bune"), ronove = new HasherA5("ronove"), berith = new HasherA5("berith"),
            astaroth = new HasherA5("astaroth"), forneus = new HasherA5("forneus"), foras = new HasherA5("foras"), asmoday = new HasherA5("asmoday"),
            gaap = new HasherA5("gaap"), furfur = new HasherA5("furfur"), marchosias = new HasherA5("marchosias"), stolas = new HasherA5("stolas"),
            phenex = new HasherA5("phenex"), halphas = new HasherA5("halphas"), malphas = new HasherA5("malphas"), raum = new HasherA5("raum"),
            focalor = new HasherA5("focalor"), vepar = new HasherA5("vepar"), sabnock = new HasherA5("sabnock"), shax = new HasherA5("shax"),
            vine = new HasherA5("vine"), bifrons = new HasherA5("bifrons"), vual = new HasherA5("vual"), haagenti = new HasherA5("haagenti"),
            crocell = new HasherA5("crocell"), furcas = new HasherA5("furcas"), balam = new HasherA5("balam"), alloces = new HasherA5("alloces"),
            caim = new HasherA5("caim"), murmur = new HasherA5("murmur"), orobas = new HasherA5("orobas"), gremory = new HasherA5("gremory"),
            ose = new HasherA5("ose"), amy = new HasherA5("amy"), orias = new HasherA5("orias"), vapula = new HasherA5("vapula"),
            zagan = new HasherA5("zagan"), valac = new HasherA5("valac"), andras = new HasherA5("andras"), flauros = new HasherA5("flauros"),
            andrealphus = new HasherA5("andrealphus"), kimaris = new HasherA5("kimaris"), amdusias = new HasherA5("amdusias"), belial = new HasherA5("belial"),
            decarabia = new HasherA5("decarabia"), seere = new HasherA5("seere"), dantalion = new HasherA5("dantalion"), andromalius = new HasherA5("andromalius"),
            baal_ = new HasherA5("BAAL"), agares_ = new HasherA5("AGARES"), vassago_ = new HasherA5("VASSAGO"), samigina_ = new HasherA5("SAMIGINA"),
            marbas_ = new HasherA5("MARBAS"), valefor_ = new HasherA5("VALEFOR"), amon_ = new HasherA5("AMON"), barbatos_ = new HasherA5("BARBATOS"),
            paimon_ = new HasherA5("PAIMON"), buer_ = new HasherA5("BUER"), gusion_ = new HasherA5("GUSION"), sitri_ = new HasherA5("SITRI"),
            beleth_ = new HasherA5("BELETH"), leraje_ = new HasherA5("LERAJE"), eligos_ = new HasherA5("ELIGOS"), zepar_ = new HasherA5("ZEPAR"),
            botis_ = new HasherA5("BOTIS"), bathin_ = new HasherA5("BATHIN"), sallos_ = new HasherA5("SALLOS"), purson_ = new HasherA5("PURSON"),
            marax_ = new HasherA5("MARAX"), ipos_ = new HasherA5("IPOS"), aim_ = new HasherA5("AIM"), naberius_ = new HasherA5("NABERIUS"),
            glasya_labolas_ = new HasherA5("GLASYA_LABOLAS"), bune_ = new HasherA5("BUNE"), ronove_ = new HasherA5("RONOVE"), berith_ = new HasherA5("BERITH"),
            astaroth_ = new HasherA5("ASTAROTH"), forneus_ = new HasherA5("FORNEUS"), foras_ = new HasherA5("FORAS"), asmoday_ = new HasherA5("ASMODAY"),
            gaap_ = new HasherA5("GAAP"), furfur_ = new HasherA5("FURFUR"), marchosias_ = new HasherA5("MARCHOSIAS"), stolas_ = new HasherA5("STOLAS"),
            phenex_ = new HasherA5("PHENEX"), halphas_ = new HasherA5("HALPHAS"), malphas_ = new HasherA5("MALPHAS"), raum_ = new HasherA5("RAUM"),
            focalor_ = new HasherA5("FOCALOR"), vepar_ = new HasherA5("VEPAR"), sabnock_ = new HasherA5("SABNOCK"), shax_ = new HasherA5("SHAX"),
            vine_ = new HasherA5("VINE"), bifrons_ = new HasherA5("BIFRONS"), vual_ = new HasherA5("VUAL"), haagenti_ = new HasherA5("HAAGENTI"),
            crocell_ = new HasherA5("CROCELL"), furcas_ = new HasherA5("FURCAS"), balam_ = new HasherA5("BALAM"), alloces_ = new HasherA5("ALLOCES"),
            caim_ = new HasherA5("CAIM"), murmur_ = new HasherA5("MURMUR"), orobas_ = new HasherA5("OROBAS"), gremory_ = new HasherA5("GREMORY"),
            ose_ = new HasherA5("OSE"), amy_ = new HasherA5("AMY"), orias_ = new HasherA5("ORIAS"), vapula_ = new HasherA5("VAPULA"),
            zagan_ = new HasherA5("ZAGAN"), valac_ = new HasherA5("VALAC"), andras_ = new HasherA5("ANDRAS"), flauros_ = new HasherA5("FLAUROS"),
            andrealphus_ = new HasherA5("ANDREALPHUS"), kimaris_ = new HasherA5("KIMARIS"), amdusias_ = new HasherA5("AMDUSIAS"), belial_ = new HasherA5("BELIAL"),
            decarabia_ = new HasherA5("DECARABIA"), seere_ = new HasherA5("SEERE"), dantalion_ = new HasherA5("DANTALION"), andromalius_ = new HasherA5("ANDROMALIUS"),
            hydrogen = new HasherA5("hydrogen"), helium = new HasherA5("helium"), lithium = new HasherA5("lithium"), beryllium = new HasherA5("beryllium"), boron = new HasherA5("boron"), carbon = new HasherA5("carbon"), nitrogen = new HasherA5("nitrogen"), oxygen = new HasherA5("oxygen"), fluorine = new HasherA5("fluorine"), neon = new HasherA5("neon"), sodium = new HasherA5("sodium"), magnesium = new HasherA5("magnesium"), aluminium = new HasherA5("aluminium"), silicon = new HasherA5("silicon"), phosphorus = new HasherA5("phosphorus"), sulfur = new HasherA5("sulfur"), chlorine = new HasherA5("chlorine"), argon = new HasherA5("argon"), potassium = new HasherA5("potassium"), calcium = new HasherA5("calcium"), scandium = new HasherA5("scandium"), titanium = new HasherA5("titanium"), vanadium = new HasherA5("vanadium"), chromium = new HasherA5("chromium"), manganese = new HasherA5("manganese"), iron = new HasherA5("iron"), cobalt = new HasherA5("cobalt"), nickel = new HasherA5("nickel"), copper = new HasherA5("copper"), zinc = new HasherA5("zinc"), gallium = new HasherA5("gallium"), germanium = new HasherA5("germanium"), arsenic = new HasherA5("arsenic"), selenium = new HasherA5("selenium"), bromine = new HasherA5("bromine"), krypton = new HasherA5("krypton"), rubidium = new HasherA5("rubidium"), strontium = new HasherA5("strontium"), yttrium = new HasherA5("yttrium"), zirconium = new HasherA5("zirconium"), niobium = new HasherA5("niobium"), molybdenum = new HasherA5("molybdenum"), technetium = new HasherA5("technetium"), ruthenium = new HasherA5("ruthenium"), rhodium = new HasherA5("rhodium"), palladium = new HasherA5("palladium"), silver = new HasherA5("silver"), cadmium = new HasherA5("cadmium"), indium = new HasherA5("indium"), tin = new HasherA5("tin"), antimony = new HasherA5("antimony"), tellurium = new HasherA5("tellurium"), iodine = new HasherA5("iodine"), xenon = new HasherA5("xenon"), caesium = new HasherA5("caesium"), barium = new HasherA5("barium"), lanthanum = new HasherA5("lanthanum"), cerium = new HasherA5("cerium"), praseodymium = new HasherA5("praseodymium"), neodymium = new HasherA5("neodymium"), promethium = new HasherA5("promethium"), samarium = new HasherA5("samarium"), europium = new HasherA5("europium"), gadolinium = new HasherA5("gadolinium"), terbium = new HasherA5("terbium"), dysprosium = new HasherA5("dysprosium"), holmium = new HasherA5("holmium"), erbium = new HasherA5("erbium"), thulium = new HasherA5("thulium"), ytterbium = new HasherA5("ytterbium"), lutetium = new HasherA5("lutetium"), hafnium = new HasherA5("hafnium"), tantalum = new HasherA5("tantalum"), tungsten = new HasherA5("tungsten"), rhenium = new HasherA5("rhenium"), osmium = new HasherA5("osmium"), iridium = new HasherA5("iridium"), platinum = new HasherA5("platinum"), gold = new HasherA5("gold"), mercury = new HasherA5("mercury"), thallium = new HasherA5("thallium"), lead = new HasherA5("lead"), bismuth = new HasherA5("bismuth"), polonium = new HasherA5("polonium"), astatine = new HasherA5("astatine"), radon = new HasherA5("radon"), francium = new HasherA5("francium"), radium = new HasherA5("radium"), actinium = new HasherA5("actinium"), thorium = new HasherA5("thorium"), protactinium = new HasherA5("protactinium"), uranium = new HasherA5("uranium"), neptunium = new HasherA5("neptunium"), plutonium = new HasherA5("plutonium"), americium = new HasherA5("americium"), curium = new HasherA5("curium"), berkelium = new HasherA5("berkelium"), californium = new HasherA5("californium"), einsteinium = new HasherA5("einsteinium"), fermium = new HasherA5("fermium"), mendelevium = new HasherA5("mendelevium"), nobelium = new HasherA5("nobelium"), lawrencium = new HasherA5("lawrencium"), rutherfordium = new HasherA5("rutherfordium"), dubnium = new HasherA5("dubnium"), seaborgium = new HasherA5("seaborgium"), bohrium = new HasherA5("bohrium"), hassium = new HasherA5("hassium"), meitnerium = new HasherA5("meitnerium"), darmstadtium = new HasherA5("darmstadtium"), roentgenium = new HasherA5("roentgenium"), copernicium = new HasherA5("copernicium"), nihonium = new HasherA5("nihonium"), flerovium = new HasherA5("flerovium"), moscovium = new HasherA5("moscovium"), livermorium = new HasherA5("livermorium"), tennessine = new HasherA5("tennessine"), oganesson = new HasherA5("oganesson"),
            hydrogen_ = new HasherA5("HYDROGEN"), helium_ = new HasherA5("HELIUM"), lithium_ = new HasherA5("LITHIUM"), beryllium_ = new HasherA5("BERYLLIUM"), boron_ = new HasherA5("BORON"), carbon_ = new HasherA5("CARBON"), nitrogen_ = new HasherA5("NITROGEN"), oxygen_ = new HasherA5("OXYGEN"), fluorine_ = new HasherA5("FLUORINE"), neon_ = new HasherA5("NEON"), sodium_ = new HasherA5("SODIUM"), magnesium_ = new HasherA5("MAGNESIUM"), aluminium_ = new HasherA5("ALUMINIUM"), silicon_ = new HasherA5("SILICON"), phosphorus_ = new HasherA5("PHOSPHORUS"), sulfur_ = new HasherA5("SULFUR"), chlorine_ = new HasherA5("CHLORINE"), argon_ = new HasherA5("ARGON"), potassium_ = new HasherA5("POTASSIUM"), calcium_ = new HasherA5("CALCIUM"), scandium_ = new HasherA5("SCANDIUM"), titanium_ = new HasherA5("TITANIUM"), vanadium_ = new HasherA5("VANADIUM"), chromium_ = new HasherA5("CHROMIUM"), manganese_ = new HasherA5("MANGANESE"), iron_ = new HasherA5("IRON"), cobalt_ = new HasherA5("COBALT"), nickel_ = new HasherA5("NICKEL"), copper_ = new HasherA5("COPPER"), zinc_ = new HasherA5("ZINC"), gallium_ = new HasherA5("GALLIUM"), germanium_ = new HasherA5("GERMANIUM"), arsenic_ = new HasherA5("ARSENIC"), selenium_ = new HasherA5("SELENIUM"), bromine_ = new HasherA5("BROMINE"), krypton_ = new HasherA5("KRYPTON"), rubidium_ = new HasherA5("RUBIDIUM"), strontium_ = new HasherA5("STRONTIUM"), yttrium_ = new HasherA5("YTTRIUM"), zirconium_ = new HasherA5("ZIRCONIUM"), niobium_ = new HasherA5("NIOBIUM"), molybdenum_ = new HasherA5("MOLYBDENUM"), technetium_ = new HasherA5("TECHNETIUM"), ruthenium_ = new HasherA5("RUTHENIUM"), rhodium_ = new HasherA5("RHODIUM"), palladium_ = new HasherA5("PALLADIUM"), silver_ = new HasherA5("SILVER"), cadmium_ = new HasherA5("CADMIUM"), indium_ = new HasherA5("INDIUM"), tin_ = new HasherA5("TIN"), antimony_ = new HasherA5("ANTIMONY"), tellurium_ = new HasherA5("TELLURIUM"), iodine_ = new HasherA5("IODINE"), xenon_ = new HasherA5("XENON"), caesium_ = new HasherA5("CAESIUM"), barium_ = new HasherA5("BARIUM"), lanthanum_ = new HasherA5("LANTHANUM"), cerium_ = new HasherA5("CERIUM"), praseodymium_ = new HasherA5("PRASEODYMIUM"), neodymium_ = new HasherA5("NEODYMIUM"), promethium_ = new HasherA5("PROMETHIUM"), samarium_ = new HasherA5("SAMARIUM"), europium_ = new HasherA5("EUROPIUM"), gadolinium_ = new HasherA5("GADOLINIUM"), terbium_ = new HasherA5("TERBIUM"), dysprosium_ = new HasherA5("DYSPROSIUM"), holmium_ = new HasherA5("HOLMIUM"), erbium_ = new HasherA5("ERBIUM"), thulium_ = new HasherA5("THULIUM"), ytterbium_ = new HasherA5("YTTERBIUM"), lutetium_ = new HasherA5("LUTETIUM"), hafnium_ = new HasherA5("HAFNIUM"), tantalum_ = new HasherA5("TANTALUM"), tungsten_ = new HasherA5("TUNGSTEN"), rhenium_ = new HasherA5("RHENIUM"), osmium_ = new HasherA5("OSMIUM"), iridium_ = new HasherA5("IRIDIUM"), platinum_ = new HasherA5("PLATINUM"), gold_ = new HasherA5("GOLD"), mercury_ = new HasherA5("MERCURY"), thallium_ = new HasherA5("THALLIUM"), lead_ = new HasherA5("LEAD"), bismuth_ = new HasherA5("BISMUTH"), polonium_ = new HasherA5("POLONIUM"), astatine_ = new HasherA5("ASTATINE"), radon_ = new HasherA5("RADON"), francium_ = new HasherA5("FRANCIUM"), radium_ = new HasherA5("RADIUM"), actinium_ = new HasherA5("ACTINIUM"), thorium_ = new HasherA5("THORIUM"), protactinium_ = new HasherA5("PROTACTINIUM"), uranium_ = new HasherA5("URANIUM"), neptunium_ = new HasherA5("NEPTUNIUM"), plutonium_ = new HasherA5("PLUTONIUM"), americium_ = new HasherA5("AMERICIUM"), curium_ = new HasherA5("CURIUM"), berkelium_ = new HasherA5("BERKELIUM"), californium_ = new HasherA5("CALIFORNIUM"), einsteinium_ = new HasherA5("EINSTEINIUM"), fermium_ = new HasherA5("FERMIUM"), mendelevium_ = new HasherA5("MENDELEVIUM"), nobelium_ = new HasherA5("NOBELIUM"), lawrencium_ = new HasherA5("LAWRENCIUM"), rutherfordium_ = new HasherA5("RUTHERFORDIUM"), dubnium_ = new HasherA5("DUBNIUM"), seaborgium_ = new HasherA5("SEABORGIUM"), bohrium_ = new HasherA5("BOHRIUM"), hassium_ = new HasherA5("HASSIUM"), meitnerium_ = new HasherA5("MEITNERIUM"), darmstadtium_ = new HasherA5("DARMSTADTIUM"), roentgenium_ = new HasherA5("ROENTGENIUM"), copernicium_ = new HasherA5("COPERNICIUM"), nihonium_ = new HasherA5("NIHONIUM"), flerovium_ = new HasherA5("FLEROVIUM"), moscovium_ = new HasherA5("MOSCOVIUM"), livermorium_ = new HasherA5("LIVERMORIUM"), tennessine_ = new HasherA5("TENNESSINE"), oganesson_ = new HasherA5("OGANESSON");

    /**
     * Has a length of 428, which may be relevant if automatically choosing a predefined hash functor.
     */
    public static final HasherA5[] predefined = new HasherA5[]{alpha, beta, gamma, delta, epsilon, zeta, eta, theta, iota,
            kappa, lambda, mu, nu, xi, omicron, pi, rho, sigma, tau, upsilon, phi, chi, psi, omega,
            alpha_, beta_, gamma_, delta_, epsilon_, zeta_, eta_, theta_, iota_,
            kappa_, lambda_, mu_, nu_, xi_, omicron_, pi_, rho_, sigma_, tau_, upsilon_, phi_, chi_, psi_, omega_,
            baal, agares, vassago, samigina, marbas, valefor, amon, barbatos,
            paimon, buer, gusion, sitri, beleth, leraje, eligos, zepar,
            botis, bathin, sallos, purson, marax, ipos, aim, naberius,
            glasya_labolas, bune, ronove, berith, astaroth, forneus, foras, asmoday,
            gaap, furfur, marchosias, stolas, phenex, halphas, malphas, raum,
            focalor, vepar, sabnock, shax, vine, bifrons, vual, haagenti,
            crocell, furcas, balam, alloces, caim, murmur, orobas, gremory,
            ose, amy, orias, vapula, zagan, valac, andras, flauros,
            andrealphus, kimaris, amdusias, belial, decarabia, seere, dantalion, andromalius,
            baal_, agares_, vassago_, samigina_, marbas_, valefor_, amon_, barbatos_,
            paimon_, buer_, gusion_, sitri_, beleth_, leraje_, eligos_, zepar_,
            botis_, bathin_, sallos_, purson_, marax_, ipos_, aim_, naberius_,
            glasya_labolas_, bune_, ronove_, berith_, astaroth_, forneus_, foras_, asmoday_,
            gaap_, furfur_, marchosias_, stolas_, phenex_, halphas_, malphas_, raum_,
            focalor_, vepar_, sabnock_, shax_, vine_, bifrons_, vual_, haagenti_,
            crocell_, furcas_, balam_, alloces_, caim_, murmur_, orobas_, gremory_,
            ose_, amy_, orias_, vapula_, zagan_, valac_, andras_, flauros_,
            andrealphus_, kimaris_, amdusias_, belial_, decarabia_, seere_, dantalion_, andromalius_,

            hydrogen, helium, lithium, beryllium, boron, carbon, nitrogen, oxygen, fluorine, neon,
            sodium, magnesium, aluminium, silicon, phosphorus, sulfur, chlorine, argon, potassium,
            calcium, scandium, titanium, vanadium, chromium, manganese, iron, cobalt, nickel,
            copper, zinc, gallium, germanium, arsenic, selenium, bromine, krypton, rubidium,
            strontium, yttrium, zirconium, niobium, molybdenum, technetium, ruthenium, rhodium,
            palladium, silver, cadmium, indium, tin, antimony, tellurium, iodine, xenon, caesium,
            barium, lanthanum, cerium, praseodymium, neodymium, promethium, samarium, europium,
            gadolinium, terbium, dysprosium, holmium, erbium, thulium, ytterbium, lutetium, hafnium,
            tantalum, tungsten, rhenium, osmium, iridium, platinum, gold, mercury, thallium, lead,
            bismuth, polonium, astatine, radon, francium, radium, actinium, thorium, protactinium,
            uranium, neptunium, plutonium, americium, curium, berkelium, californium, einsteinium,
            fermium, mendelevium, nobelium, lawrencium, rutherfordium, dubnium, seaborgium, bohrium,
            hassium, meitnerium, darmstadtium, roentgenium, copernicium, nihonium, flerovium, moscovium,
            livermorium, tennessine, oganesson,

            hydrogen_, helium_, lithium_, beryllium_, boron_, carbon_, nitrogen_, oxygen_, fluorine_, neon_,
            sodium_, magnesium_, aluminium_, silicon_, phosphorus_, sulfur_, chlorine_, argon_, potassium_,
            calcium_, scandium_, titanium_, vanadium_, chromium_, manganese_, iron_, cobalt_, nickel_,
            copper_, zinc_, gallium_, germanium_, arsenic_, selenium_, bromine_, krypton_, rubidium_,
            strontium_, yttrium_, zirconium_, niobium_, molybdenum_, technetium_, ruthenium_, rhodium_,
            palladium_, silver_, cadmium_, indium_, tin_, antimony_, tellurium_, iodine_, xenon_, caesium_,
            barium_, lanthanum_, cerium_, praseodymium_, neodymium_, promethium_, samarium_, europium_,
            gadolinium_, terbium_, dysprosium_, holmium_, erbium_, thulium_, ytterbium_, lutetium_, hafnium_,
            tantalum_, tungsten_, rhenium_, osmium_, iridium_, platinum_, gold_, mercury_, thallium_, lead_,
            bismuth_, polonium_, astatine_, radon_, francium_, radium_, actinium_, thorium_, protactinium_,
            uranium_, neptunium_, plutonium_, americium_, curium_, berkelium_, californium_, einsteinium_,
            fermium_, mendelevium_, nobelium_, lawrencium_, rutherfordium_, dubnium_, seaborgium_, bohrium_,
            hassium_, meitnerium_, darmstadtium_, roentgenium_, copernicium_, nihonium_, flerovium_, moscovium_,
            livermorium_, tennessine_, oganesson_,
    };
//</editor-fold>
//<editor-fold defaultstate="collapsed" desc="Instance Methods">

    /**
     * A hashing function that is meant for smaller input arrays.
     *
     * @param data input array
     * @return the 32-bit hash of data
     */
    public int hashA5(final int[] data) {
        if (data == null) return 0;
        return hashA5(data, 0, data.length);
    }

    /**
     * A hashing function that is meant for smaller input arrays.
     *
     * @param data   input array
     * @param start  starting index in data
     * @param length how many items to use from data
     * @return the 32-bit hash of data
     */
    public int hashA5(final int[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        int len = Math.min(length, data.length - start);
        int val01 = VAL01;
        int val10 = VAL10;

        int seed1 = 0x243F6A88 ^ len;
        int seed2 = 0x85A308D3 ^ len;
        int seed3 = 0xFB0BD3EA;
        int seed4 = 0x0F58FD47;
        int a, b, c, d;
        long p;

        p = (seed2 ^ (seed & 0xFFFFFFFFL)) * (seed1 ^ (seed >>> 32));
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);
        p = (seed3 ^ (seed & 0xFFFFFFFFL)) * (seed4 ^ (seed >>> 32));
        seed3 = (int) p;
        seed4 = (int) (p >>> 32);

        int i = start;
        if (len <= 4) {
            if (len == 0) {
                a = 0;
                b = 0;
            } else {
                int last = i + len - 1;
                a = data[i];
                b = data[last];
                if (len > 2) {
                    int mo = len >>> 1;
                    c = data[i + mo];
                    d = data[last - mo];
                    p = ((long) seed3 + c) * ((long) seed4 + d);
                    seed3 = (int) p;
                    seed4 = (int) (p >>> 32);
                }
            }
        } else {
            val01 ^= seed1;
            val10 ^= seed2;

            do {
                final int s1 = seed1;
                final int s4 = seed4;

                p = ((long) seed1 + data[i]) * ((long) seed2 + data[i + 1]);
                seed1 = (int) p;
                seed2 = (int) (p >>> 32);
                p = ((long) seed3 + data[i + 2]) * ((long) seed4 + data[i + 3]);
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);

                len -= 4;
                i += 4;

                seed1 += val01;
                seed2 += s4;
                seed3 += s1;
                seed4 += val10;
            } while (len > 4);

            a = data[i + len - 2];
            b = data[i + len - 1];

            if (len > 2) {
                c = data[i + len - 4];
                d = data[i + len - 3];

                p = ((long) seed3 + c) * ((long) seed4 + d);
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);
            }
        }
        seed1 ^= seed3;
        seed2 ^= seed4;

        p = ((long) seed1 + a) * ((long) seed2 + b);
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);

        p = ((long) seed1 ^ val01) * ((long) seed2);
        a = (int) p;
        b = (int) (p >>> 32);

        return a ^ b;
    }

    /**
     * A hashing function that is meant for smaller input arrays.
     *
     * @param data input array
     * @return the 32-bit hash of data
     */
    public int hashA5(final long[] data) {
        if (data == null) return 0;
        return hashA5(data, 0, data.length);
    }

    /**
     * A hashing function that is meant for smaller input arrays.
     *
     * @param data   input array
     * @param start  starting index in data
     * @param length how many items to use from data
     * @return the 32-bit hash of data
     */
    public int hashA5(final long[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        int len = Math.min(length, data.length - start);
        int val01 = VAL01;
        int val10 = VAL10;

        int seed1 = 0x243F6A88 ^ len;
        int seed2 = 0x85A308D3 ^ len;
        int seed3 = 0xFB0BD3EA;
        int seed4 = 0x0F58FD47;
        long a, b, c, d;
        long p;

        p = (seed2 ^ (seed & 0xFFFFFFFFL)) * (seed1 ^ (seed >>> 32));
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);
        p = (seed3 ^ (seed & 0xFFFFFFFFL)) * (seed4 ^ (seed >>> 32));
        seed3 = (int) p;
        seed4 = (int) (p >>> 32);

        int i = start;
        switch (len) {
            case 0:
                a = 0L;
                b = 0L;
                break;
            case 2:
                p = data[i + 1];
                c = p & 0xFFFFFFFFL;
                d = (p >>> 32);

                p = (seed3 + c) * (seed4 + d);
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);
                // intentional fallthrough
            case 1:
                p = data[i];
                a = (p & 0xFFFFFFFFL);
                b = (p >>> 32);
                break;
            default:
                val01 ^= seed1;
                val10 ^= seed2;

                do {
                    final int s1 = seed1;
                    final int s4 = seed4;

                    p = data[i];
                    p = (seed1 + (p & 0xFFFFFFFFL)) * (seed2 + (p >>> 32));
                    seed1 = (int) p;
                    seed2 = (int) (p >>> 32);
                    p = data[i + 1];
                    p = (seed3 + (p & 0xFFFFFFFFL)) * (seed4 + (p >>> 32));
                    seed3 = (int) p;
                    seed4 = (int) (p >>> 32);

                    len -= 2;
                    i += 2;

                    seed1 += val01;
                    seed2 += s4;
                    seed3 += s1;
                    seed4 += val10;
                } while (len > 2);

                p = data[i + len - 1];
                a = (p & 0xFFFFFFFFL);
                b = (p >>> 32);

                if (len > 1) {
                    p = data[i + len - 2];
                    c = (p & 0xFFFFFFFFL);
                    d = (p >>> 32);

                    p = (seed3 + c) * (seed4 + d);
                    seed3 = (int) p;
                    seed4 = (int) (p >>> 32);
                }
                break;
        }
        seed1 ^= seed3;
        seed2 ^= seed4;

        p = (seed1 + a) * (seed2 + b);
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);

        p = ((long) seed1 ^ val01) * ((long) seed2);
        a = (p & 0xFFFFFFFFL);
        b = (p >>> 32);

        return (int) (a ^ b);
    }

    /**
     * A hashing function that is meant for smaller input arrays.
     *
     * @param data input array
     * @return the 32-bit hash of data
     */
    public int hashA5(final boolean[] data) {
        if (data == null) return 0;
        return hashA5(data, 0, data.length);
    }

    /**
     * A hashing function that is meant for smaller input arrays.
     *
     * @param data   input array
     * @param start  starting index in data
     * @param length how many items to use from data
     * @return the 32-bit hash of data
     */
    public int hashA5(final boolean[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        int len = Math.min(length, data.length - start);
        int val01 = VAL01;
        int val10 = VAL10;

        int seed1 = 0x243F6A88 ^ len;
        int seed2 = 0x85A308D3 ^ len;
        int seed3 = 0xFB0BD3EA;
        int seed4 = 0x0F58FD47;
        int a, b, c, d;
        long p;

        p = (seed2 ^ (seed & 0xFFFFFFFFL)) * (seed1 ^ (seed >>> 32));
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);
        p = (seed3 ^ (seed & 0xFFFFFFFFL)) * (seed4 ^ (seed >>> 32));
        seed3 = (int) p;
        seed4 = (int) (p >>> 32);

        int i = start;
        if (len <= 4) {
            if (len == 0) {
                a = 0;
                b = 0;
            } else {
                int last = i + len - 1;
                a = data[i] ? -1 : 0;
                b = data[last] ? -1 : 0;
                if (len > 2) {
                    int mo = len >>> 1;
                    c = data[i + mo] ? -1 : 0;
                    d = data[last - mo] ? -1 : 0;
                    p = ((long) seed3 + c) * ((long) seed4 + d);
                    seed3 = (int) p;
                    seed4 = (int) (p >>> 32);
                }
            }
        } else {
            val01 ^= seed1;
            val10 ^= seed2;

            do {
                final int s1 = seed1;
                final int s4 = seed4;

                p = ((long) seed1 + (data[i] ? -1 : 0)) * ((long) seed2 + (data[i + 1] ? -1 : 0));
                seed1 = (int) p;
                seed2 = (int) (p >>> 32);
                p = ((long) seed3 + (data[i + 2] ? -1 : 0)) * ((long) seed4 + (data[i + 3] ? -1 : 0));
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);

                len -= 4;
                i += 4;

                seed1 += val01;
                seed2 += s4;
                seed3 += s1;
                seed4 += val10;
            } while (len > 4);

            a = data[i + len - 2] ? -1 : 0;
            b = data[i + len - 1] ? -1 : 0;

            if (len > 2) {
                c = data[i + len - 4] ? -1 : 0;
                d = data[i + len - 3] ? -1 : 0;

                p = ((long) seed3 + c) * ((long) seed4 + d);
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);
            }
        }
        seed1 ^= seed3;
        seed2 ^= seed4;

        p = ((long) seed1 + a) * ((long) seed2 + b);
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);

        p = ((long) seed1 ^ val01) * ((long) seed2);
        a = (int) p;
        b = (int) (p >>> 32);

        return a ^ b;
    }

    /**
     * A hashing function that is meant for smaller input arrays.
     *
     * @param data input array
     * @return the 32-bit hash of data
     */
    public int hashA5(final byte[] data) {
        if (data == null) return 0;
        return hashA5(data, 0, data.length);
    }

    /**
     * A hashing function that is meant for smaller input arrays.
     *
     * @param data   input array
     * @param start  starting index in data
     * @param length how many items to use from data
     * @return the 32-bit hash of data
     */
    public int hashA5(final byte[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        int len = Math.min(length, data.length - start);
        int val01 = VAL01;
        int val10 = VAL10;

        int seed1 = 0x243F6A88 ^ len;
        int seed2 = 0x85A308D3 ^ len;
        int seed3 = 0xFB0BD3EA;
        int seed4 = 0x0F58FD47;
        int a, b, c, d;
        long p;

        p = (seed2 ^ (seed & 0xFFFFFFFFL)) * (seed1 ^ (seed >>> 32));
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);
        p = (seed3 ^ (seed & 0xFFFFFFFFL)) * (seed4 ^ (seed >>> 32));
        seed3 = (int) p;
        seed4 = (int) (p >>> 32);

        int i = start;
        if (len <= 4) {
            if (len == 0) {
                a = 0;
                b = 0;
            } else {
                int last = i + len - 1;
                a = data[i];
                b = data[last];
                if (len > 2) {
                    int mo = len >>> 1;
                    c = data[i + mo];
                    d = data[last - mo];
                    p = ((long) seed3 + c) * ((long) seed4 + d);
                    seed3 = (int) p;
                    seed4 = (int) (p >>> 32);
                }
            }
        } else {
            val01 ^= seed1;
            val10 ^= seed2;

            do {
                final int s1 = seed1;
                final int s4 = seed4;

                p = ((long) seed1 + data[i]) * ((long) seed2 + data[i + 1]);
                seed1 = (int) p;
                seed2 = (int) (p >>> 32);
                p = ((long) seed3 + data[i + 2]) * ((long) seed4 + data[i + 3]);
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);

                len -= 4;
                i += 4;

                seed1 += val01;
                seed2 += s4;
                seed3 += s1;
                seed4 += val10;
            } while (len > 4);

            a = data[i + len - 2];
            b = data[i + len - 1];

            if (len > 2) {
                c = data[i + len - 4];
                d = data[i + len - 3];

                p = ((long) seed3 + c) * ((long) seed4 + d);
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);
            }
        }
        seed1 ^= seed3;
        seed2 ^= seed4;

        p = ((long) seed1 + a) * ((long) seed2 + b);
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);

        p = ((long) seed1 ^ val01) * ((long) seed2);
        a = (int) p;
        b = (int) (p >>> 32);

        return a ^ b;
    }

    /**
     * A hashing function that is meant for smaller input arrays.
     *
     * @param data input array
     * @return the 32-bit hash of data
     */
    public int hashA5(final short[] data) {
        if (data == null) return 0;
        return hashA5(data, 0, data.length);
    }

    /**
     * A hashing function that is meant for smaller input arrays.
     *
     * @param data   input array
     * @param start  starting index in data
     * @param length how many items to use from data
     * @return the 32-bit hash of data
     */
    public int hashA5(final short[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        int len = Math.min(length, data.length - start);
        int val01 = VAL01;
        int val10 = VAL10;

        int seed1 = 0x243F6A88 ^ len;
        int seed2 = 0x85A308D3 ^ len;
        int seed3 = 0xFB0BD3EA;
        int seed4 = 0x0F58FD47;
        int a, b, c, d;
        long p;

        p = (seed2 ^ (seed & 0xFFFFFFFFL)) * (seed1 ^ (seed >>> 32));
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);
        p = (seed3 ^ (seed & 0xFFFFFFFFL)) * (seed4 ^ (seed >>> 32));
        seed3 = (int) p;
        seed4 = (int) (p >>> 32);

        int i = start;
        if (len <= 4) {
            if (len == 0) {
                a = 0;
                b = 0;
            } else {
                int last = i + len - 1;
                a = data[i];
                b = data[last];
                if (len > 2) {
                    int mo = len >>> 1;
                    c = data[i + mo];
                    d = data[last - mo];
                    p = ((long) seed3 + c) * ((long) seed4 + d);
                    seed3 = (int) p;
                    seed4 = (int) (p >>> 32);
                }
            }
        } else {
            val01 ^= seed1;
            val10 ^= seed2;

            do {
                final int s1 = seed1;
                final int s4 = seed4;

                p = ((long) seed1 + data[i]) * ((long) seed2 + data[i + 1]);
                seed1 = (int) p;
                seed2 = (int) (p >>> 32);
                p = ((long) seed3 + data[i + 2]) * ((long) seed4 + data[i + 3]);
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);

                len -= 4;
                i += 4;

                seed1 += val01;
                seed2 += s4;
                seed3 += s1;
                seed4 += val10;
            } while (len > 4);

            a = data[i + len - 2];
            b = data[i + len - 1];

            if (len > 2) {
                c = data[i + len - 4];
                d = data[i + len - 3];

                p = ((long) seed3 + c) * ((long) seed4 + d);
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);
            }
        }
        seed1 ^= seed3;
        seed2 ^= seed4;

        p = ((long) seed1 + a) * ((long) seed2 + b);
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);

        p = ((long) seed1 ^ val01) * ((long) seed2);
        a = (int) p;
        b = (int) (p >>> 32);

        return a ^ b;
    }

    /**
     * A hashing function that is meant for smaller input arrays.
     *
     * @param data input array
     * @return the 32-bit hash of data
     */
    public int hashA5(final char[] data) {
        if (data == null) return 0;
        return hashA5(data, 0, data.length);
    }

    /**
     * A hashing function that is meant for smaller input arrays.
     *
     * @param data   input array
     * @param start  starting index in data
     * @param length how many items to use from data
     * @return the 32-bit hash of data
     */
    public int hashA5(final char[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        int len = Math.min(length, data.length - start);
        int val01 = VAL01;
        int val10 = VAL10;

        int seed1 = 0x243F6A88 ^ len;
        int seed2 = 0x85A308D3 ^ len;
        int seed3 = 0xFB0BD3EA;
        int seed4 = 0x0F58FD47;
        int a, b, c, d;
        long p;

        p = (seed2 ^ (seed & 0xFFFFFFFFL)) * (seed1 ^ (seed >>> 32));
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);
        p = (seed3 ^ (seed & 0xFFFFFFFFL)) * (seed4 ^ (seed >>> 32));
        seed3 = (int) p;
        seed4 = (int) (p >>> 32);

        int i = start;
        if (len <= 4) {
            if (len == 0) {
                a = 0;
                b = 0;
            } else {
                int last = i + len - 1;
                a = data[i];
                b = data[last];
                if (len > 2) {
                    int mo = len >>> 1;
                    c = data[i + mo];
                    d = data[last - mo];
                    p = ((long) seed3 + c) * ((long) seed4 + d);
                    seed3 = (int) p;
                    seed4 = (int) (p >>> 32);
                }
            }
        } else {
            val01 ^= seed1;
            val10 ^= seed2;

            do {
                final int s1 = seed1;
                final int s4 = seed4;

                p = ((long) seed1 + data[i]) * ((long) seed2 + data[i + 1]);
                seed1 = (int) p;
                seed2 = (int) (p >>> 32);
                p = ((long) seed3 + data[i + 2]) * ((long) seed4 + data[i + 3]);
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);

                len -= 4;
                i += 4;

                seed1 += val01;
                seed2 += s4;
                seed3 += s1;
                seed4 += val10;
            } while (len > 4);

            a = data[i + len - 2];
            b = data[i + len - 1];

            if (len > 2) {
                c = data[i + len - 4];
                d = data[i + len - 3];

                p = ((long) seed3 + c) * ((long) seed4 + d);
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);
            }
        }
        seed1 ^= seed3;
        seed2 ^= seed4;

        p = ((long) seed1 + a) * ((long) seed2 + b);
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);

        p = ((long) seed1 ^ val01) * ((long) seed2);
        a = (int) p;
        b = (int) (p >>> 32);

        return a ^ b;
    }

    /**
     * A hashing function that is meant for smaller input arrays.
     *
     * @param data input array
     * @return the 32-bit hash of data
     */
    public int hashA5(final float[] data) {
        if (data == null) return 0;
        return hashA5(data, 0, data.length);
    }

    /**
     * A hashing function that is meant for smaller input arrays.
     *
     * @param data   input array
     * @param start  starting index in data
     * @param length how many items to use from data
     * @return the 32-bit hash of data
     */
    public int hashA5(final float[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        int len = Math.min(length, data.length - start);
        int val01 = VAL01;
        int val10 = VAL10;

        int seed1 = 0x243F6A88 ^ len;
        int seed2 = 0x85A308D3 ^ len;
        int seed3 = 0xFB0BD3EA;
        int seed4 = 0x0F58FD47;
        int a, b, c, d;
        long p;

        p = (seed2 ^ (seed & 0xFFFFFFFFL)) * (seed1 ^ (seed >>> 32));
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);
        p = (seed3 ^ (seed & 0xFFFFFFFFL)) * (seed4 ^ (seed >>> 32));
        seed3 = (int) p;
        seed4 = (int) (p >>> 32);

        int i = start;
        if (len <= 4) {
            if (len == 0) {
                a = 0;
                b = 0;
            } else {
                int last = i + len - 1;
                a = BitConversion.floatToRawIntBits(data[i]);
                b = BitConversion.floatToRawIntBits(data[last]);
                if (len > 2) {
                    int mo = len >>> 1;
                    c = BitConversion.floatToRawIntBits(data[i + mo]);
                    d = BitConversion.floatToRawIntBits(data[last - mo]);
                    p = ((long) seed3 + c) * ((long) seed4 + d);
                    seed3 = (int) p;
                    seed4 = (int) (p >>> 32);
                }
            }
        } else {
            val01 ^= seed1;
            val10 ^= seed2;

            do {
                final int s1 = seed1;
                final int s4 = seed4;

                p = ((long) seed1 + BitConversion.floatToRawIntBits(data[i])) * ((long) seed2 + BitConversion.floatToRawIntBits(data[i + 1]));
                seed1 = (int) p;
                seed2 = (int) (p >>> 32);
                p = ((long) seed3 + BitConversion.floatToRawIntBits(data[i + 2])) * ((long) seed4 + BitConversion.floatToRawIntBits(data[i + 3]));
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);

                len -= 4;
                i += 4;

                seed1 += val01;
                seed2 += s4;
                seed3 += s1;
                seed4 += val10;
            } while (len > 4);

            a = BitConversion.floatToRawIntBits(data[i + len - 2]);
            b = BitConversion.floatToRawIntBits(data[i + len - 1]);

            if (len > 2) {
                c = BitConversion.floatToRawIntBits(data[i + len - 4]);
                d = BitConversion.floatToRawIntBits(data[i + len - 3]);

                p = ((long) seed3 + c) * ((long) seed4 + d);
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);
            }
        }
        seed1 ^= seed3;
        seed2 ^= seed4;

        p = ((long) seed1 + a) * ((long) seed2 + b);
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);

        p = ((long) seed1 ^ val01) * ((long) seed2);
        a = (int) p;
        b = (int) (p >>> 32);

        return a ^ b;
    }

    /**
     * A hashing function that is meant for smaller input arrays.
     *
     * @param data input array
     * @return the 32-bit hash of data
     */
    public int hashA5(final double[] data) {
        if (data == null) return 0;
        return hashA5(data, 0, data.length);
    }

    /**
     * A hashing function that is meant for smaller input arrays.
     *
     * @param data   input array
     * @param start  starting index in data
     * @param length how many items to use from data
     * @return the 32-bit hash of data
     */
    public int hashA5(final double[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        int len = Math.min(length, data.length - start);
        int val01 = VAL01;
        int val10 = VAL10;

        int seed1 = 0x243F6A88 ^ len;
        int seed2 = 0x85A308D3 ^ len;
        int seed3 = 0xFB0BD3EA;
        int seed4 = 0x0F58FD47;
        int a, b, c, d;
        long p;

        p = (seed2 ^ (seed & 0xFFFFFFFFL)) * (seed1 ^ (seed >>> 32));
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);
        p = (seed3 ^ (seed & 0xFFFFFFFFL)) * (seed4 ^ (seed >>> 32));
        seed3 = (int) p;
        seed4 = (int) (p >>> 32);

        int i = start;
        if (len <= 4) {
            if (len == 0) {
                a = 0;
                b = 0;
            } else {
                int last = i + len - 1;
                a = BitConversion.doubleToMixedIntBits(data[i]);
                b = BitConversion.doubleToMixedIntBits(data[last]);
                if (len > 2) {
                    int mo = len >>> 1;
                    c = BitConversion.doubleToMixedIntBits(data[i + mo]);
                    d = BitConversion.doubleToMixedIntBits(data[last - mo]);
                    p = ((long) seed3 + c) * ((long) seed4 + d);
                    seed3 = (int) p;
                    seed4 = (int) (p >>> 32);
                }
            }
        } else {
            val01 ^= seed1;
            val10 ^= seed2;

            do {
                final int s1 = seed1;
                final int s4 = seed4;

                p = ((long) seed1 + BitConversion.doubleToMixedIntBits(data[i])) * ((long) seed2 + BitConversion.doubleToMixedIntBits(data[i + 1]));
                seed1 = (int) p;
                seed2 = (int) (p >>> 32);
                p = ((long) seed3 + BitConversion.doubleToMixedIntBits(data[i + 2])) * ((long) seed4 + BitConversion.doubleToMixedIntBits(data[i + 3]));
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);

                len -= 4;
                i += 4;

                seed1 += val01;
                seed2 += s4;
                seed3 += s1;
                seed4 += val10;
            } while (len > 4);

            a = BitConversion.doubleToMixedIntBits(data[i + len - 2]);
            b = BitConversion.doubleToMixedIntBits(data[i + len - 1]);

            if (len > 2) {
                c = BitConversion.doubleToMixedIntBits(data[i + len - 4]);
                d = BitConversion.doubleToMixedIntBits(data[i + len - 3]);

                p = ((long) seed3 + c) * ((long) seed4 + d);
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);
            }
        }
        seed1 ^= seed3;
        seed2 ^= seed4;

        p = ((long) seed1 + a) * ((long) seed2 + b);
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);

        p = ((long) seed1 ^ val01) * ((long) seed2);
        a = (int) p;
        b = (int) (p >>> 32);

        return a ^ b;
    }

    /**
     * A hashing function that is meant for smaller input CharSequences.
     *
     * @param data input CharSequence
     * @return the 32-bit hash of data
     */
    public int hashA5(final CharSequence data) {
        if (data == null) return 0;
        return hashA5(data, 0, data.length());
    }

    /**
     * A hashing function that is meant for smaller input CharSequences.
     *
     * @param data   input CharSequence
     * @param start  starting index in data
     * @param length how many items to use from data
     * @return the 32-bit hash of data
     */
    public int hashA5(final CharSequence data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length())
            return 0;
        int len = Math.min(length, data.length() - start);
        int val01 = VAL01;
        int val10 = VAL10;

        int seed1 = 0x243F6A88 ^ len;
        int seed2 = 0x85A308D3 ^ len;
        int seed3 = 0xFB0BD3EA;
        int seed4 = 0x0F58FD47;
        int a, b, c, d;
        long p;

        p = (seed2 ^ (seed & 0xFFFFFFFFL)) * (seed1 ^ (seed >>> 32));
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);
        p = (seed3 ^ (seed & 0xFFFFFFFFL)) * (seed4 ^ (seed >>> 32));
        seed3 = (int) p;
        seed4 = (int) (p >>> 32);

        int i = start;
        if (len <= 4) {
            if (len == 0) {
                a = 0;
                b = 0;
            } else {
                int last = i + len - 1;
                a = data.charAt(i);
                b = data.charAt(last);
                if (len > 2) {
                    int mo = len >>> 1;
                    c = data.charAt(i + mo);
                    d = data.charAt(last - mo);
                    p = ((long) seed3 + c) * ((long) seed4 + d);
                    seed3 = (int) p;
                    seed4 = (int) (p >>> 32);
                }
            }
        } else {
            val01 ^= seed1;
            val10 ^= seed2;

            do {
                final int s1 = seed1;
                final int s4 = seed4;

                p = ((long) seed1 + data.charAt(i)) * ((long) seed2 + data.charAt(i + 1));
                seed1 = (int) p;
                seed2 = (int) (p >>> 32);
                p = ((long) seed3 + data.charAt(i + 2)) * ((long) seed4 + data.charAt(i + 3));
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);

                len -= 4;
                i += 4;

                seed1 += val01;
                seed2 += s4;
                seed3 += s1;
                seed4 += val10;
            } while (len > 4);

            a = data.charAt(i + len - 2);
            b = data.charAt(i + len - 1);

            if (len > 2) {
                c = data.charAt(i + len - 4);
                d = data.charAt(i + len - 3);

                p = ((long) seed3 + c) * ((long) seed4 + d);
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);
            }
        }
        seed1 ^= seed3;
        seed2 ^= seed4;

        p = ((long) seed1 + a) * ((long) seed2 + b);
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);

        p = ((long) seed1 ^ val01) * ((long) seed2);
        a = (int) p;
        b = (int) (p >>> 32);

        return a ^ b;
    }

    /**
     * A hashing function that is meant for smaller input Strings.
     *
     * @param data input String
     * @return the 32-bit hash of data
     */
    public int hashA5(final String data) {
        if (data == null) return 0;
        return hashA5(data, 0, data.length());
    }

    /**
     * A hashing function that is meant for smaller input Strings.
     *
     * @param data   input String
     * @param start  starting index in data
     * @param length how many items to use from data
     * @return the 32-bit hash of data
     */
    public int hashA5(final String data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length())
            return 0;
        int len = Math.min(length, data.length() - start);
        int val01 = VAL01;
        int val10 = VAL10;

        int seed1 = 0x243F6A88 ^ len;
        int seed2 = 0x85A308D3 ^ len;
        int seed3 = 0xFB0BD3EA;
        int seed4 = 0x0F58FD47;
        int a, b, c, d;
        long p;

        p = (seed2 ^ (seed & 0xFFFFFFFFL)) * (seed1 ^ (seed >>> 32));
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);
        p = (seed3 ^ (seed & 0xFFFFFFFFL)) * (seed4 ^ (seed >>> 32));
        seed3 = (int) p;
        seed4 = (int) (p >>> 32);

        int i = start;
        if (len <= 4) {
            if (len == 0) {
                a = 0;
                b = 0;
            } else {
                int last = i + len - 1;
                a = data.charAt(i);
                b = data.charAt(last);
                if (len > 2) {
                    int mo = len >>> 1;
                    c = data.charAt(i + mo);
                    d = data.charAt(last - mo);
                    p = ((long) seed3 + c) * ((long) seed4 + d);
                    seed3 = (int) p;
                    seed4 = (int) (p >>> 32);
                }
            }
        } else {
            val01 ^= seed1;
            val10 ^= seed2;

            do {
                final int s1 = seed1;
                final int s4 = seed4;

                p = ((long) seed1 + data.charAt(i)) * ((long) seed2 + data.charAt(i + 1));
                seed1 = (int) p;
                seed2 = (int) (p >>> 32);
                p = ((long) seed3 + data.charAt(i + 2)) * ((long) seed4 + data.charAt(i + 3));
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);

                len -= 4;
                i += 4;

                seed1 += val01;
                seed2 += s4;
                seed3 += s1;
                seed4 += val10;
            } while (len > 4);

            a = data.charAt(i + len - 2);
            b = data.charAt(i + len - 1);

            if (len > 2) {
                c = data.charAt(i + len - 4);
                d = data.charAt(i + len - 3);

                p = ((long) seed3 + c) * ((long) seed4 + d);
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);
            }
        }
        seed1 ^= seed3;
        seed2 ^= seed4;

        p = ((long) seed1 + a) * ((long) seed2 + b);
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);

        p = ((long) seed1 ^ val01) * ((long) seed2);
        a = (int) p;
        b = (int) (p >>> 32);

        return a ^ b;
    }

    /**
     * A hashing function that is meant for smaller input arrays.
     *
     * @param data input array
     * @return the 32-bit hash of data
     */
    public int hashA5(final Object[] data) {
        if (data == null) return 0;
        return hashA5(data, 0, data.length);
    }

    /**
     * A hashing function that is meant for smaller input arrays.
     *
     * @param data   input array
     * @param start  starting index in data
     * @param length how many items to use from data
     * @return the 32-bit hash of data
     */
    public int hashA5(final Object[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        int len = Math.min(length, data.length - start);
        int val01 = VAL01;
        int val10 = VAL10;

        int seed1 = 0x243F6A88 ^ len;
        int seed2 = 0x85A308D3 ^ len;
        int seed3 = 0xFB0BD3EA;
        int seed4 = 0x0F58FD47;
        int a, b, c, d;
        long p;

        p = (seed2 ^ (seed & 0xFFFFFFFFL)) * (seed1 ^ (seed >>> 32));
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);
        p = (seed3 ^ (seed & 0xFFFFFFFFL)) * (seed4 ^ (seed >>> 32));
        seed3 = (int) p;
        seed4 = (int) (p >>> 32);

        int i = start;
        if (len <= 4) {
            if (len == 0) {
                a = 0;
                b = 0;
            } else {
                int last = i + len - 1;
                a = Objects.hashCode(data[i]);
                b = Objects.hashCode(data[last]);
                if (len > 2) {
                    int mo = len >>> 1;
                    c = Objects.hashCode(data[i + mo]);
                    d = Objects.hashCode(data[last - mo]);
                    p = ((long) seed3 + c) * ((long) seed4 + d);
                    seed3 = (int) p;
                    seed4 = (int) (p >>> 32);
                }
            }
        } else {
            val01 ^= seed1;
            val10 ^= seed2;

            do {
                final int s1 = seed1;
                final int s4 = seed4;

                p = ((long) seed1 + Objects.hashCode(data[i])) * ((long) seed2 + Objects.hashCode(data[i + 1]));
                seed1 = (int) p;
                seed2 = (int) (p >>> 32);
                p = ((long) seed3 + Objects.hashCode(data[i + 2])) * ((long) seed4 + Objects.hashCode(data[i + 3]));
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);

                len -= 4;
                i += 4;

                seed1 += val01;
                seed2 += s4;
                seed3 += s1;
                seed4 += val10;
            } while (len > 4);

            a = Objects.hashCode(data[i + len - 2]);
            b = Objects.hashCode(data[i + len - 1]);

            if (len > 2) {
                c = Objects.hashCode(data[i + len - 4]);
                d = Objects.hashCode(data[i + len - 3]);

                p = ((long) seed3 + c) * ((long) seed4 + d);
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);
            }
        }
        seed1 ^= seed3;
        seed2 ^= seed4;

        p = ((long) seed1 + a) * ((long) seed2 + b);
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);

        p = ((long) seed1 ^ val01) * ((long) seed2);
        a = (int) p;
        b = (int) (p >>> 32);

        return a ^ b;
    }

    /**
     * A hashing function that is meant for smaller input ByteBuffers.
     *
     * @param data input ByteBuffer
     * @return the 32-bit hash of data
     */
    public int hashA5(final ByteBuffer data) {
        if (data == null) return 0;
        return hashA5(data, 0, data.limit());
    }

    /**
     * A hashing function that is meant for smaller input ByteBuffers.
     *
     * @param data   input ByteBuffer
     * @param start  starting index in data
     * @param length how many items to use from data
     * @return the 32-bit hash of data
     */
    public int hashA5(final ByteBuffer data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.limit())
            return 0;
        int len = Math.min(length, data.limit() - start);
        int val01 = VAL01;
        int val10 = VAL10;

        int seed1 = 0x243F6A88 ^ len;
        int seed2 = 0x85A308D3 ^ len;
        int seed3 = 0xFB0BD3EA;
        int seed4 = 0x0F58FD47;
        int a, b, c, d;
        long p;

        p = (seed2 ^ (seed & 0xFFFFFFFFL)) * (seed1 ^ (seed >>> 32));
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);
        p = (seed3 ^ (seed & 0xFFFFFFFFL)) * (seed4 ^ (seed >>> 32));
        seed3 = (int) p;
        seed4 = (int) (p >>> 32);

        int i = start;
        if (len <= 16) {
            if (len > 3) {
                int last = i + len - 4;
                a = data.getInt(i);
                b = data.getInt(last);
                if (len > 8) {
                    int mo = (len >>> 3) << 2;
                    c = data.getInt(i + mo);
                    d = data.getInt(last - mo);
                    p = ((long) seed3 + c) * ((long) seed4 + d);
                    seed3 = (int) p;
                    seed4 = (int) (p >>> 32);
                }
            } else {
                a = 0;
                b = 0;
                if (len != 0) {
                    a = data.get(i);
                    if (len != 1) {
                        a ^= data.get(i + 1) << 8;
                        if (len != 2) {
                            a ^= data.get(i + 2) << 16;
                        }
                    }
                }

            }
        } else {
            val01 ^= seed1;
            val10 ^= seed2;

            do {
                final int s1 = seed1;
                final int s4 = seed4;

                p = ((long) seed1 + data.getInt(i)) * ((long) seed2 + data.getInt(i + 4));
                seed1 = (int) p;
                seed2 = (int) (p >>> 32);
                p = ((long) seed3 + data.getInt(i + 8)) * ((long) seed4 + data.getInt(i + 12));
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);

                len -= 16;
                i += 16;

                seed1 += val01;
                seed2 += s4;
                seed3 += s1;
                seed4 += val10;
            } while (len > 16);

            a = data.getInt(i + len - 8);
            b = data.getInt(i + len - 4);

            if (len > 8) {
                c = data.getInt(i + len - 16);
                d = data.getInt(i + len - 12);

                p = ((long) seed3 + c) * ((long) seed4 + d);
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);
            }
        }
        seed1 ^= seed3;
        seed2 ^= seed4;

        p = ((long) seed1 + a) * ((long) seed2 + b);
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);

        p = ((long) seed1 ^ val01) * ((long) seed2);
        a = (int) p;
        b = (int) (p >>> 32);

        return a ^ b;
    }

    /**
     * Meant to handle hashing larger 2D arrays (or higher dimensions), this lets you pass a {@link Hasher.HashFunction} as
     * the first parameter, and then this uses that function to get a hash for each T item in data. T is usually an
     * array type, and function is usually a method reference to a {@link #hashA5} method here.
     *
     * @param function typically a method reference to a {@link #hashA5} method here
     * @param data     input array
     * @param <T>      typically an array type, often of primitive items; may be more than one-dimensional
     * @return the 32-bit hash of data
     */
    public <T> int hashA5(final Hasher.HashFunction<T> function, final T[] data) {
        if (data == null) return 0;
        return hashA5(function, data, 0, data.length);
    }

    /**
     * Meant to handle hashing larger 2D arrays (or higher dimensions), this lets you pass a {@link Hasher.HashFunction} as
     * the first parameter, and then this uses that function to get a hash for each T item in data. T is usually an
     * array type, and function is usually a method reference to a {@link #hashA5} method here.
     *
     * @param function typically a method reference to a {@link #hashA5} method here
     * @param data     input array
     * @param start    starting index in data
     * @param length   how many items to use from data
     * @param <T>      typically an array type, often of primitive items; may be more than one-dimensional
     * @return the 32-bit hash of data
     */
    public <T> int hashA5(final Hasher.HashFunction<T> function, final T[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        int len = Math.min(length, data.length - start);
        int val01 = VAL01;
        int val10 = VAL10;

        int seed1 = 0x243F6A88 ^ len;
        int seed2 = 0x85A308D3 ^ len;
        int seed3 = 0xFB0BD3EA;
        int seed4 = 0x0F58FD47;
        int a, b, c, d;
        long p;

        p = (seed2 ^ (seed & 0xFFFFFFFFL)) * (seed1 ^ (seed >>> 32));
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);
        p = (seed3 ^ (seed & 0xFFFFFFFFL)) * (seed4 ^ (seed >>> 32));
        seed3 = (int) p;
        seed4 = (int) (p >>> 32);

        int i = start;
        if (len <= 4) {
            if (len == 0) {
                a = 0;
                b = 0;
            } else {
                int last = i + len - 1;
                a = function.hash(data[i]);
                b = function.hash(data[last]);
                if (len > 2) {
                    int mo = len >>> 1;
                    c = function.hash(data[i + mo]);
                    d = function.hash(data[last - mo]);
                    p = ((long) seed3 + c) * ((long) seed4 + d);
                    seed3 = (int) p;
                    seed4 = (int) (p >>> 32);
                }
            }
        } else {
            val01 ^= seed1;
            val10 ^= seed2;

            do {
                final int s1 = seed1;
                final int s4 = seed4;

                p = ((long) seed1 + function.hash(data[i])) * ((long) seed2 + function.hash(data[i + 1]));
                seed1 = (int) p;
                seed2 = (int) (p >>> 32);
                p = ((long) seed3 + function.hash(data[i + 2])) * ((long) seed4 + function.hash(data[i + 3]));
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);

                len -= 4;
                i += 4;

                seed1 += val01;
                seed2 += s4;
                seed3 += s1;
                seed4 += val10;
            } while (len > 4);

            a = function.hash(data[i + len - 2]);
            b = function.hash(data[i + len - 1]);

            if (len > 2) {
                c = function.hash(data[i + len - 4]);
                d = function.hash(data[i + len - 3]);

                p = ((long) seed3 + c) * ((long) seed4 + d);
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);
            }
        }
        seed1 ^= seed3;
        seed2 ^= seed4;

        p = ((long) seed1 + a) * ((long) seed2 + b);
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);

        p = ((long) seed1 ^ val01) * ((long) seed2);
        a = (int) p;
        b = (int) (p >>> 32);

        return a ^ b;
    }

//</editor-fold>
//<editor-fold defaultstate="collapsed" desc="Static Methods">

    /**
     * A hashing function that is meant for smaller input arrays.
     *
     * @param seed any long seed
     * @param data input array
     * @return the 32-bit hash of data
     */
    public static int hashA5(final long seed, final int[] data) {
        if (data == null) return 0;
        return hashA5(seed, data, 0, data.length);
    }

    /**
     * A hashing function that is meant for smaller input arrays.
     *
     * @param seed any long seed
     * @param data   input array
     * @param start  starting index in data
     * @param length how many items to use from data
     * @return the 32-bit hash of data
     */
    public static int hashA5(final long seed, final int[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        int len = Math.min(length, data.length - start);
        int val01 = VAL01;
        int val10 = VAL10;

        int seed1 = 0x243F6A88 ^ len;
        int seed2 = 0x85A308D3 ^ len;
        int seed3 = 0xFB0BD3EA;
        int seed4 = 0x0F58FD47;
        int a, b, c, d;
        long p;

        p = (seed2 ^ (seed & 0xFFFFFFFFL)) * (seed1 ^ (seed >>> 32));
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);
        p = (seed3 ^ (seed & 0xFFFFFFFFL)) * (seed4 ^ (seed >>> 32));
        seed3 = (int) p;
        seed4 = (int) (p >>> 32);

        int i = start;
        if (len <= 4) {
            if (len == 0) {
                a = 0;
                b = 0;
            } else {
                int last = i + len - 1;
                a = data[i];
                b = data[last];
                if (len > 2) {
                    int mo = len >>> 1;
                    c = data[i + mo];
                    d = data[last - mo];
                    p = ((long) seed3 + c) * ((long) seed4 + d);
                    seed3 = (int) p;
                    seed4 = (int) (p >>> 32);
                }
            }
        } else {
            val01 ^= seed1;
            val10 ^= seed2;

            do {
                final int s1 = seed1;
                final int s4 = seed4;

                p = ((long) seed1 + data[i]) * ((long) seed2 + data[i + 1]);
                seed1 = (int) p;
                seed2 = (int) (p >>> 32);
                p = ((long) seed3 + data[i + 2]) * ((long) seed4 + data[i + 3]);
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);

                len -= 4;
                i += 4;

                seed1 += val01;
                seed2 += s4;
                seed3 += s1;
                seed4 += val10;
            } while (len > 4);

            a = data[i + len - 2];
            b = data[i + len - 1];

            if (len > 2) {
                c = data[i + len - 4];
                d = data[i + len - 3];

                p = ((long) seed3 + c) * ((long) seed4 + d);
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);
            }
        }
        seed1 ^= seed3;
        seed2 ^= seed4;

        p = ((long) seed1 + a) * ((long) seed2 + b);
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);

        p = ((long) seed1 ^ val01) * ((long) seed2);
        a = (int) p;
        b = (int) (p >>> 32);

        return a ^ b;
    }

    /**
     * A hashing function that is meant for smaller input arrays.
     *
     * @param seed any long seed
     * @param data input array
     * @return the 32-bit hash of data
     */
    public static int hashA5(final long seed, final long[] data) {
        if (data == null) return 0;
        return hashA5(seed, data, 0, data.length);
    }

    /**
     * A hashing function that is meant for smaller input arrays.
     *
     * @param seed any long seed
     * @param data   input array
     * @param start  starting index in data
     * @param length how many items to use from data
     * @return the 32-bit hash of data
     */
    public static int hashA5(final long seed, final long[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        int len = Math.min(length, data.length - start);
        int val01 = VAL01;
        int val10 = VAL10;

        int seed1 = 0x243F6A88 ^ len;
        int seed2 = 0x85A308D3 ^ len;
        int seed3 = 0xFB0BD3EA;
        int seed4 = 0x0F58FD47;
        long a, b, c, d;
        long p;

        p = (seed2 ^ (seed & 0xFFFFFFFFL)) * (seed1 ^ (seed >>> 32));
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);
        p = (seed3 ^ (seed & 0xFFFFFFFFL)) * (seed4 ^ (seed >>> 32));
        seed3 = (int) p;
        seed4 = (int) (p >>> 32);

        int i = start;
        switch (len) {
            case 0:
                a = 0L;
                b = 0L;
                break;
            case 2:
                p = data[i + 1];
                c = p & 0xFFFFFFFFL;
                d = (p >>> 32);

                p = (seed3 + c) * (seed4 + d);
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);
                // intentional fallthrough
            case 1:
                p = data[i];
                a = (p & 0xFFFFFFFFL);
                b = (p >>> 32);
                break;
            default:
                val01 ^= seed1;
                val10 ^= seed2;

                do {
                    final int s1 = seed1;
                    final int s4 = seed4;

                    p = data[i];
                    p = (seed1 + (p & 0xFFFFFFFFL)) * (seed2 + (p >>> 32));
                    seed1 = (int) p;
                    seed2 = (int) (p >>> 32);
                    p = data[i + 1];
                    p = (seed3 + (p & 0xFFFFFFFFL)) * (seed4 + (p >>> 32));
                    seed3 = (int) p;
                    seed4 = (int) (p >>> 32);

                    len -= 2;
                    i += 2;

                    seed1 += val01;
                    seed2 += s4;
                    seed3 += s1;
                    seed4 += val10;
                } while (len > 2);

                p = data[i + len - 1];
                a = (p & 0xFFFFFFFFL);
                b = (p >>> 32);

                if (len > 1) {
                    p = data[i + len - 2];
                    c = (p & 0xFFFFFFFFL);
                    d = (p >>> 32);

                    p = (seed3 + c) * (seed4 + d);
                    seed3 = (int) p;
                    seed4 = (int) (p >>> 32);
                }
                break;
        }
        seed1 ^= seed3;
        seed2 ^= seed4;

        p = (seed1 + a) * (seed2 + b);
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);

        p = ((long) seed1 ^ val01) * ((long) seed2);
        a = (p & 0xFFFFFFFFL);
        b = (p >>> 32);

        return (int) (a ^ b);
    }
    /**
     * A hashing function that is meant for smaller input arrays.
     *
     * @param seed any long seed
     * @param data input array
     * @return the 32-bit hash of data
     */
    public static int hashA5(final long seed, final boolean[] data) {
        if (data == null) return 0;
        return hashA5(seed, data, 0, data.length);
    }

    /**
     * A hashing function that is meant for smaller input arrays.
     *
     * @param seed any long seed
     * @param data   input array
     * @param start  starting index in data
     * @param length how many items to use from data
     * @return the 32-bit hash of data
     */
    public static int hashA5(final long seed, final boolean[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        int len = Math.min(length, data.length - start);
        int val01 = VAL01;
        int val10 = VAL10;

        int seed1 = 0x243F6A88 ^ len;
        int seed2 = 0x85A308D3 ^ len;
        int seed3 = 0xFB0BD3EA;
        int seed4 = 0x0F58FD47;
        int a, b, c, d;
        long p;

        p = (seed2 ^ (seed & 0xFFFFFFFFL)) * (seed1 ^ (seed >>> 32));
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);
        p = (seed3 ^ (seed & 0xFFFFFFFFL)) * (seed4 ^ (seed >>> 32));
        seed3 = (int) p;
        seed4 = (int) (p >>> 32);

        int i = start;
        if (len <= 4) {
            if (len == 0) {
                a = 0;
                b = 0;
            } else {
                int last = i + len - 1;
                a = data[i] ? -1 : 0;
                b = data[last] ? -1 : 0;
                if (len > 2) {
                    int mo = len >>> 1;
                    c = data[i + mo] ? -1 : 0;
                    d = data[last - mo] ? -1 : 0;
                    p = ((long) seed3 + c) * ((long) seed4 + d);
                    seed3 = (int) p;
                    seed4 = (int) (p >>> 32);
                }
            }
        } else {
            val01 ^= seed1;
            val10 ^= seed2;

            do {
                final int s1 = seed1;
                final int s4 = seed4;

                p = ((long) seed1 + (data[i] ? -1 : 0)) * ((long) seed2 + (data[i + 1] ? -1 : 0));
                seed1 = (int) p;
                seed2 = (int) (p >>> 32);
                p = ((long) seed3 + (data[i + 2] ? -1 : 0)) * ((long) seed4 + (data[i + 3] ? -1 : 0));
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);

                len -= 4;
                i += 4;

                seed1 += val01;
                seed2 += s4;
                seed3 += s1;
                seed4 += val10;
            } while (len > 4);

            a = data[i + len - 2] ? -1 : 0;
            b = data[i + len - 1] ? -1 : 0;

            if (len > 2) {
                c = data[i + len - 4] ? -1 : 0;
                d = data[i + len - 3] ? -1 : 0;

                p = ((long) seed3 + c) * ((long) seed4 + d);
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);
            }
        }
        seed1 ^= seed3;
        seed2 ^= seed4;

        p = ((long) seed1 + a) * ((long) seed2 + b);
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);

        p = ((long) seed1 ^ val01) * ((long) seed2);
        a = (int) p;
        b = (int) (p >>> 32);

        return a ^ b;
    }

    /**
     * A hashing function that is meant for smaller input arrays.
     *
     * @param data input array
     * @return the 32-bit hash of data
     */
    public static int hashA5(final long seed, final byte[] data) {
        if (data == null) return 0;
        return hashA5(seed, data, 0, data.length);
    }

    /**
     * A hashing function that is meant for smaller input arrays.
     *
     * @param data   input array
     * @param start  starting index in data
     * @param length how many items to use from data
     * @return the 32-bit hash of data
     */
    public static int hashA5(final long seed, final byte[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        int len = Math.min(length, data.length - start);
        int val01 = VAL01;
        int val10 = VAL10;

        int seed1 = 0x243F6A88 ^ len;
        int seed2 = 0x85A308D3 ^ len;
        int seed3 = 0xFB0BD3EA;
        int seed4 = 0x0F58FD47;
        int a, b, c, d;
        long p;

        p = (seed2 ^ (seed & 0xFFFFFFFFL)) * (seed1 ^ (seed >>> 32));
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);
        p = (seed3 ^ (seed & 0xFFFFFFFFL)) * (seed4 ^ (seed >>> 32));
        seed3 = (int) p;
        seed4 = (int) (p >>> 32);

        int i = start;
        if (len <= 4) {
            if (len == 0) {
                a = 0;
                b = 0;
            } else {
                int last = i + len - 1;
                a = data[i];
                b = data[last];
                if (len > 2) {
                    int mo = len >>> 1;
                    c = data[i + mo];
                    d = data[last - mo];
                    p = ((long) seed3 + c) * ((long) seed4 + d);
                    seed3 = (int) p;
                    seed4 = (int) (p >>> 32);
                }
            }
        } else {
            val01 ^= seed1;
            val10 ^= seed2;

            do {
                final int s1 = seed1;
                final int s4 = seed4;

                p = ((long) seed1 + data[i]) * ((long) seed2 + data[i + 1]);
                seed1 = (int) p;
                seed2 = (int) (p >>> 32);
                p = ((long) seed3 + data[i + 2]) * ((long) seed4 + data[i + 3]);
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);

                len -= 4;
                i += 4;

                seed1 += val01;
                seed2 += s4;
                seed3 += s1;
                seed4 += val10;
            } while (len > 4);

            a = data[i + len - 2];
            b = data[i + len - 1];

            if (len > 2) {
                c = data[i + len - 4];
                d = data[i + len - 3];

                p = ((long) seed3 + c) * ((long) seed4 + d);
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);
            }
        }
        seed1 ^= seed3;
        seed2 ^= seed4;

        p = ((long) seed1 + a) * ((long) seed2 + b);
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);

        p = ((long) seed1 ^ val01) * ((long) seed2);
        a = (int) p;
        b = (int) (p >>> 32);

        return a ^ b;
    }

    /**
     * A hashing function that is meant for smaller input arrays.
     *
     * @param seed any long seed
     * @param data input array
     * @return the 32-bit hash of data
     */
    public static int hashA5(final long seed, final short[] data) {
        if (data == null) return 0;
        return hashA5(seed, data, 0, data.length);
    }

    /**
     * A hashing function that is meant for smaller input arrays.
     *
     * @param seed any long seed
     * @param data   input array
     * @param start  starting index in data
     * @param length how many items to use from data
     * @return the 32-bit hash of data
     */
    public static int hashA5(final long seed, final short[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        int len = Math.min(length, data.length - start);
        int val01 = VAL01;
        int val10 = VAL10;

        int seed1 = 0x243F6A88 ^ len;
        int seed2 = 0x85A308D3 ^ len;
        int seed3 = 0xFB0BD3EA;
        int seed4 = 0x0F58FD47;
        int a, b, c, d;
        long p;

        p = (seed2 ^ (seed & 0xFFFFFFFFL)) * (seed1 ^ (seed >>> 32));
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);
        p = (seed3 ^ (seed & 0xFFFFFFFFL)) * (seed4 ^ (seed >>> 32));
        seed3 = (int) p;
        seed4 = (int) (p >>> 32);

        int i = start;
        if (len <= 4) {
            if (len == 0) {
                a = 0;
                b = 0;
            } else {
                int last = i + len - 1;
                a = data[i];
                b = data[last];
                if (len > 2) {
                    int mo = len >>> 1;
                    c = data[i + mo];
                    d = data[last - mo];
                    p = ((long) seed3 + c) * ((long) seed4 + d);
                    seed3 = (int) p;
                    seed4 = (int) (p >>> 32);
                }
            }
        } else {
            val01 ^= seed1;
            val10 ^= seed2;

            do {
                final int s1 = seed1;
                final int s4 = seed4;

                p = ((long) seed1 + data[i]) * ((long) seed2 + data[i + 1]);
                seed1 = (int) p;
                seed2 = (int) (p >>> 32);
                p = ((long) seed3 + data[i + 2]) * ((long) seed4 + data[i + 3]);
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);

                len -= 4;
                i += 4;

                seed1 += val01;
                seed2 += s4;
                seed3 += s1;
                seed4 += val10;
            } while (len > 4);

            a = data[i + len - 2];
            b = data[i + len - 1];

            if (len > 2) {
                c = data[i + len - 4];
                d = data[i + len - 3];

                p = ((long) seed3 + c) * ((long) seed4 + d);
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);
            }
        }
        seed1 ^= seed3;
        seed2 ^= seed4;

        p = ((long) seed1 + a) * ((long) seed2 + b);
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);

        p = ((long) seed1 ^ val01) * ((long) seed2);
        a = (int) p;
        b = (int) (p >>> 32);

        return a ^ b;
    }

    /**
     * A hashing function that is meant for smaller input arrays.
     *
     * @param seed any long seed
     * @param data input array
     * @return the 32-bit hash of data
     */
    public static int hashA5(final long seed, final char[] data) {
        if (data == null) return 0;
        return hashA5(seed, data, 0, data.length);
    }

    /**
     * A hashing function that is meant for smaller input arrays.
     *
     * @param seed any long seed
     * @param data   input array
     * @param start  starting index in data
     * @param length how many items to use from data
     * @return the 32-bit hash of data
     */
    public static int hashA5(final long seed, final char[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        int len = Math.min(length, data.length - start);
        int val01 = VAL01;
        int val10 = VAL10;

        int seed1 = 0x243F6A88 ^ len;
        int seed2 = 0x85A308D3 ^ len;
        int seed3 = 0xFB0BD3EA;
        int seed4 = 0x0F58FD47;
        int a, b, c, d;
        long p;

        p = (seed2 ^ (seed & 0xFFFFFFFFL)) * (seed1 ^ (seed >>> 32));
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);
        p = (seed3 ^ (seed & 0xFFFFFFFFL)) * (seed4 ^ (seed >>> 32));
        seed3 = (int) p;
        seed4 = (int) (p >>> 32);

        int i = start;
        if (len <= 4) {
            if (len == 0) {
                a = 0;
                b = 0;
            } else {
                int last = i + len - 1;
                a = data[i];
                b = data[last];
                if (len > 2) {
                    int mo = len >>> 1;
                    c = data[i + mo];
                    d = data[last - mo];
                    p = ((long) seed3 + c) * ((long) seed4 + d);
                    seed3 = (int) p;
                    seed4 = (int) (p >>> 32);
                }
            }
        } else {
            val01 ^= seed1;
            val10 ^= seed2;

            do {
                final int s1 = seed1;
                final int s4 = seed4;

                p = ((long) seed1 + data[i]) * ((long) seed2 + data[i + 1]);
                seed1 = (int) p;
                seed2 = (int) (p >>> 32);
                p = ((long) seed3 + data[i + 2]) * ((long) seed4 + data[i + 3]);
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);

                len -= 4;
                i += 4;

                seed1 += val01;
                seed2 += s4;
                seed3 += s1;
                seed4 += val10;
            } while (len > 4);

            a = data[i + len - 2];
            b = data[i + len - 1];

            if (len > 2) {
                c = data[i + len - 4];
                d = data[i + len - 3];

                p = ((long) seed3 + c) * ((long) seed4 + d);
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);
            }
        }
        seed1 ^= seed3;
        seed2 ^= seed4;

        p = ((long) seed1 + a) * ((long) seed2 + b);
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);

        p = ((long) seed1 ^ val01) * ((long) seed2);
        a = (int) p;
        b = (int) (p >>> 32);

        return a ^ b;
    }

    /**
     * A hashing function that is meant for smaller input arrays.
     *
     * @param seed any long seed
     * @param data input array
     * @return the 32-bit hash of data
     */
    public static int hashA5(final long seed, final float[] data) {
        if (data == null) return 0;
        return hashA5(seed, data, 0, data.length);
    }

    /**
     * A hashing function that is meant for smaller input arrays.
     *
     * @param seed any long seed
     * @param data   input array
     * @param start  starting index in data
     * @param length how many items to use from data
     * @return the 32-bit hash of data
     */
    public static int hashA5(final long seed, final float[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        int len = Math.min(length, data.length - start);
        int val01 = VAL01;
        int val10 = VAL10;

        int seed1 = 0x243F6A88 ^ len;
        int seed2 = 0x85A308D3 ^ len;
        int seed3 = 0xFB0BD3EA;
        int seed4 = 0x0F58FD47;
        int a, b, c, d;
        long p;

        p = (seed2 ^ (seed & 0xFFFFFFFFL)) * (seed1 ^ (seed >>> 32));
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);
        p = (seed3 ^ (seed & 0xFFFFFFFFL)) * (seed4 ^ (seed >>> 32));
        seed3 = (int) p;
        seed4 = (int) (p >>> 32);

        int i = start;
        if (len <= 4) {
            if (len == 0) {
                a = 0;
                b = 0;
            } else {
                int last = i + len - 1;
                a = BitConversion.floatToRawIntBits(data[i]);
                b = BitConversion.floatToRawIntBits(data[last]);
                if (len > 2) {
                    int mo = len >>> 1;
                    c = BitConversion.floatToRawIntBits(data[i + mo]);
                    d = BitConversion.floatToRawIntBits(data[last - mo]);
                    p = ((long) seed3 + c) * ((long) seed4 + d);
                    seed3 = (int) p;
                    seed4 = (int) (p >>> 32);
                }
            }
        } else {
            val01 ^= seed1;
            val10 ^= seed2;

            do {
                final int s1 = seed1;
                final int s4 = seed4;

                p = ((long) seed1 + BitConversion.floatToRawIntBits(data[i])) * ((long) seed2 + BitConversion.floatToRawIntBits(data[i + 1]));
                seed1 = (int) p;
                seed2 = (int) (p >>> 32);
                p = ((long) seed3 + BitConversion.floatToRawIntBits(data[i + 2])) * ((long) seed4 + BitConversion.floatToRawIntBits(data[i + 3]));
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);

                len -= 4;
                i += 4;

                seed1 += val01;
                seed2 += s4;
                seed3 += s1;
                seed4 += val10;
            } while (len > 4);

            a = BitConversion.floatToRawIntBits(data[i + len - 2]);
            b = BitConversion.floatToRawIntBits(data[i + len - 1]);

            if (len > 2) {
                c = BitConversion.floatToRawIntBits(data[i + len - 4]);
                d = BitConversion.floatToRawIntBits(data[i + len - 3]);

                p = ((long) seed3 + c) * ((long) seed4 + d);
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);
            }
        }
        seed1 ^= seed3;
        seed2 ^= seed4;

        p = ((long) seed1 + a) * ((long) seed2 + b);
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);

        p = ((long) seed1 ^ val01) * ((long) seed2);
        a = (int) p;
        b = (int) (p >>> 32);

        return a ^ b;
    }

    /**
     * A hashing function that is meant for smaller input arrays.
     *
     * @param seed any long seed
     * @param data input array
     * @return the 32-bit hash of data
     */
    public static int hashA5(final long seed, final double[] data) {
        if (data == null) return 0;
        return hashA5(seed, data, 0, data.length);
    }

    /**
     * A hashing function that is meant for smaller input arrays.
     *
     * @param seed any long seed
     * @param data   input array
     * @param start  starting index in data
     * @param length how many items to use from data
     * @return the 32-bit hash of data
     */
    public static int hashA5(final long seed, final double[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        int len = Math.min(length, data.length - start);
        int val01 = VAL01;
        int val10 = VAL10;

        int seed1 = 0x243F6A88 ^ len;
        int seed2 = 0x85A308D3 ^ len;
        int seed3 = 0xFB0BD3EA;
        int seed4 = 0x0F58FD47;
        int a, b, c, d;
        long p;

        p = (seed2 ^ (seed & 0xFFFFFFFFL)) * (seed1 ^ (seed >>> 32));
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);
        p = (seed3 ^ (seed & 0xFFFFFFFFL)) * (seed4 ^ (seed >>> 32));
        seed3 = (int) p;
        seed4 = (int) (p >>> 32);

        int i = start;
        if (len <= 4) {
            if (len == 0) {
                a = 0;
                b = 0;
            } else {
                int last = i + len - 1;
                a = BitConversion.doubleToMixedIntBits(data[i]);
                b = BitConversion.doubleToMixedIntBits(data[last]);
                if (len > 2) {
                    int mo = len >>> 1;
                    c = BitConversion.doubleToMixedIntBits(data[i + mo]);
                    d = BitConversion.doubleToMixedIntBits(data[last - mo]);
                    p = ((long) seed3 + c) * ((long) seed4 + d);
                    seed3 = (int) p;
                    seed4 = (int) (p >>> 32);
                }
            }
        } else {
            val01 ^= seed1;
            val10 ^= seed2;

            do {
                final int s1 = seed1;
                final int s4 = seed4;

                p = ((long) seed1 + BitConversion.doubleToMixedIntBits(data[i])) * ((long) seed2 + BitConversion.doubleToMixedIntBits(data[i + 1]));
                seed1 = (int) p;
                seed2 = (int) (p >>> 32);
                p = ((long) seed3 + BitConversion.doubleToMixedIntBits(data[i + 2])) * ((long) seed4 + BitConversion.doubleToMixedIntBits(data[i + 3]));
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);

                len -= 4;
                i += 4;

                seed1 += val01;
                seed2 += s4;
                seed3 += s1;
                seed4 += val10;
            } while (len > 4);

            a = BitConversion.doubleToMixedIntBits(data[i + len - 2]);
            b = BitConversion.doubleToMixedIntBits(data[i + len - 1]);

            if (len > 2) {
                c = BitConversion.doubleToMixedIntBits(data[i + len - 4]);
                d = BitConversion.doubleToMixedIntBits(data[i + len - 3]);

                p = ((long) seed3 + c) * ((long) seed4 + d);
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);
            }
        }
        seed1 ^= seed3;
        seed2 ^= seed4;

        p = ((long) seed1 + a) * ((long) seed2 + b);
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);

        p = ((long) seed1 ^ val01) * ((long) seed2);
        a = (int) p;
        b = (int) (p >>> 32);

        return a ^ b;
    }

    /**
     * A hashing function that is meant for smaller input CharSequences.
     *
     * @param seed any long seed
     * @param data input CharSequence
     * @return the 32-bit hash of data
     */
    public static int hashA5(final long seed, final CharSequence data) {
        if (data == null) return 0;
        return hashA5(seed, data, 0, data.length());
    }

    /**
     * A hashing function that is meant for smaller input CharSequences.
     *
     * @param seed any long seed
     * @param data   input CharSequence
     * @param start  starting index in data
     * @param length how many items to use from data
     * @return the 32-bit hash of data
     */
    public static int hashA5(final long seed, final CharSequence data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length())
            return 0;
        int len = Math.min(length, data.length() - start);
        int val01 = VAL01;
        int val10 = VAL10;

        int seed1 = 0x243F6A88 ^ len;
        int seed2 = 0x85A308D3 ^ len;
        int seed3 = 0xFB0BD3EA;
        int seed4 = 0x0F58FD47;
        int a, b, c, d;
        long p;

        p = (seed2 ^ (seed & 0xFFFFFFFFL)) * (seed1 ^ (seed >>> 32));
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);
        p = (seed3 ^ (seed & 0xFFFFFFFFL)) * (seed4 ^ (seed >>> 32));
        seed3 = (int) p;
        seed4 = (int) (p >>> 32);

        int i = start;
        if (len <= 4) {
            if (len == 0) {
                a = 0;
                b = 0;
            } else {
                int last = i + len - 1;
                a = data.charAt(i);
                b = data.charAt(last);
                if (len > 2) {
                    int mo = len >>> 1;
                    c = data.charAt(i + mo);
                    d = data.charAt(last - mo);
                    p = ((long) seed3 + c) * ((long) seed4 + d);
                    seed3 = (int) p;
                    seed4 = (int) (p >>> 32);
                }
            }
        } else {
            val01 ^= seed1;
            val10 ^= seed2;

            do {
                final int s1 = seed1;
                final int s4 = seed4;

                p = ((long) seed1 + data.charAt(i)) * ((long) seed2 + data.charAt(i + 1));
                seed1 = (int) p;
                seed2 = (int) (p >>> 32);
                p = ((long) seed3 + data.charAt(i + 2)) * ((long) seed4 + data.charAt(i + 3));
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);

                len -= 4;
                i += 4;

                seed1 += val01;
                seed2 += s4;
                seed3 += s1;
                seed4 += val10;
            } while (len > 4);

            a = data.charAt(i + len - 2);
            b = data.charAt(i + len - 1);

            if (len > 2) {
                c = data.charAt(i + len - 4);
                d = data.charAt(i + len - 3);

                p = ((long) seed3 + c) * ((long) seed4 + d);
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);
            }
        }
        seed1 ^= seed3;
        seed2 ^= seed4;

        p = ((long) seed1 + a) * ((long) seed2 + b);
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);

        p = ((long) seed1 ^ val01) * ((long) seed2);
        a = (int) p;
        b = (int) (p >>> 32);

        return a ^ b;
    }

    /**
     * A hashing function that is meant for smaller input Strings.
     *
     * @param seed any long seed
     * @param data input String
     * @return the 32-bit hash of data
     */
    public static int hashA5(final long seed, final String data) {
        if (data == null) return 0;
        return hashA5(seed, data, 0, data.length());
    }

    /**
     * A hashing function that is meant for smaller input Strings.
     *
     * @param seed any long seed
     * @param data   input String
     * @param start  starting index in data
     * @param length how many items to use from data
     * @return the 32-bit hash of data
     */
    public static int hashA5(final long seed, final String data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length())
            return 0;
        int len = Math.min(length, data.length() - start);
        int val01 = VAL01;
        int val10 = VAL10;

        int seed1 = 0x243F6A88 ^ len;
        int seed2 = 0x85A308D3 ^ len;
        int seed3 = 0xFB0BD3EA;
        int seed4 = 0x0F58FD47;
        int a, b, c, d;
        long p;

        p = (seed2 ^ (seed & 0xFFFFFFFFL)) * (seed1 ^ (seed >>> 32));
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);
        p = (seed3 ^ (seed & 0xFFFFFFFFL)) * (seed4 ^ (seed >>> 32));
        seed3 = (int) p;
        seed4 = (int) (p >>> 32);

        int i = start;
        if (len <= 4) {
            if (len == 0) {
                a = 0;
                b = 0;
            } else {
                int last = i + len - 1;
                a = data.charAt(i);
                b = data.charAt(last);
                if (len > 2) {
                    int mo = len >>> 1;
                    c = data.charAt(i + mo);
                    d = data.charAt(last - mo);
                    p = ((long) seed3 + c) * ((long) seed4 + d);
                    seed3 = (int) p;
                    seed4 = (int) (p >>> 32);
                }
            }
        } else {
            val01 ^= seed1;
            val10 ^= seed2;

            do {
                final int s1 = seed1;
                final int s4 = seed4;

                p = ((long) seed1 + data.charAt(i)) * ((long) seed2 + data.charAt(i + 1));
                seed1 = (int) p;
                seed2 = (int) (p >>> 32);
                p = ((long) seed3 + data.charAt(i + 2)) * ((long) seed4 + data.charAt(i + 3));
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);

                len -= 4;
                i += 4;

                seed1 += val01;
                seed2 += s4;
                seed3 += s1;
                seed4 += val10;
            } while (len > 4);

            a = data.charAt(i + len - 2);
            b = data.charAt(i + len - 1);

            if (len > 2) {
                c = data.charAt(i + len - 4);
                d = data.charAt(i + len - 3);

                p = ((long) seed3 + c) * ((long) seed4 + d);
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);
            }
        }
        seed1 ^= seed3;
        seed2 ^= seed4;

        p = ((long) seed1 + a) * ((long) seed2 + b);
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);

        p = ((long) seed1 ^ val01) * ((long) seed2);
        a = (int) p;
        b = (int) (p >>> 32);

        return a ^ b;
    }

    /**
     * A hashing function that is meant for smaller input arrays.
     *
     * @param seed any long seed
     * @param data input array
     * @return the 32-bit hash of data
     */
    public static int hashA5(final long seed, final Object[] data) {
        if (data == null) return 0;
        return hashA5(seed, data, 0, data.length);
    }

    /**
     * A hashing function that is meant for smaller input arrays.
     *
     * @param data   input array
     * @param seed any long seed
     * @param start  starting index in data
     * @param length how many items to use from data
     * @return the 32-bit hash of data
     */
    public static int hashA5(final long seed, final Object[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        int len = Math.min(length, data.length - start);
        int val01 = VAL01;
        int val10 = VAL10;

        int seed1 = 0x243F6A88 ^ len;
        int seed2 = 0x85A308D3 ^ len;
        int seed3 = 0xFB0BD3EA;
        int seed4 = 0x0F58FD47;
        int a, b, c, d;
        long p;

        p = (seed2 ^ (seed & 0xFFFFFFFFL)) * (seed1 ^ (seed >>> 32));
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);
        p = (seed3 ^ (seed & 0xFFFFFFFFL)) * (seed4 ^ (seed >>> 32));
        seed3 = (int) p;
        seed4 = (int) (p >>> 32);

        int i = start;
        if (len <= 4) {
            if (len == 0) {
                a = 0;
                b = 0;
            } else {
                int last = i + len - 1;
                a = Objects.hashCode(data[i]);
                b = Objects.hashCode(data[last]);
                if (len > 2) {
                    int mo = len >>> 1;
                    c = Objects.hashCode(data[i + mo]);
                    d = Objects.hashCode(data[last - mo]);
                    p = ((long) seed3 + c) * ((long) seed4 + d);
                    seed3 = (int) p;
                    seed4 = (int) (p >>> 32);
                }
            }
        } else {
            val01 ^= seed1;
            val10 ^= seed2;

            do {
                final int s1 = seed1;
                final int s4 = seed4;

                p = ((long) seed1 + Objects.hashCode(data[i])) * ((long) seed2 + Objects.hashCode(data[i + 1]));
                seed1 = (int) p;
                seed2 = (int) (p >>> 32);
                p = ((long) seed3 + Objects.hashCode(data[i + 2])) * ((long) seed4 + Objects.hashCode(data[i + 3]));
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);

                len -= 4;
                i += 4;

                seed1 += val01;
                seed2 += s4;
                seed3 += s1;
                seed4 += val10;
            } while (len > 4);

            a = Objects.hashCode(data[i + len - 2]);
            b = Objects.hashCode(data[i + len - 1]);

            if (len > 2) {
                c = Objects.hashCode(data[i + len - 4]);
                d = Objects.hashCode(data[i + len - 3]);

                p = ((long) seed3 + c) * ((long) seed4 + d);
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);
            }
        }
        seed1 ^= seed3;
        seed2 ^= seed4;

        p = ((long) seed1 + a) * ((long) seed2 + b);
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);

        p = ((long) seed1 ^ val01) * ((long) seed2);
        a = (int) p;
        b = (int) (p >>> 32);

        return a ^ b;
    }

    /**
     * A hashing function that is meant for smaller input ByteBuffers.
     *
     * @param seed any long seed
     * @param data input ByteBuffer
     * @return the 32-bit hash of data
     */
    public static int hashA5(final long seed, final ByteBuffer data) {
        if (data == null) return 0;
        return hashA5(seed, data, 0, data.limit());
    }

    /**
     * A hashing function that is meant for smaller input ByteBuffers.
     *
     * @param seed any long seed
     * @param data   input ByteBuffer
     * @param start  starting index in data
     * @param length how many items to use from data
     * @return the 32-bit hash of data
     */
    public static int hashA5(final long seed, final ByteBuffer data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.limit())
            return 0;
        int len = Math.min(length, data.limit() - start);
        int val01 = VAL01;
        int val10 = VAL10;

        int seed1 = 0x243F6A88 ^ len;
        int seed2 = 0x85A308D3 ^ len;
        int seed3 = 0xFB0BD3EA;
        int seed4 = 0x0F58FD47;
        int a, b, c, d;
        long p;

        p = (seed2 ^ (seed & 0xFFFFFFFFL)) * (seed1 ^ (seed >>> 32));
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);
        p = (seed3 ^ (seed & 0xFFFFFFFFL)) * (seed4 ^ (seed >>> 32));
        seed3 = (int) p;
        seed4 = (int) (p >>> 32);

        int i = start;
        if (len <= 16) {
            if (len > 3) {
                int last = i + len - 4;
                a = data.getInt(i);
                b = data.getInt(last);
                if (len > 8) {
                    int mo = (len >>> 3) << 2;
                    c = data.getInt(i + mo);
                    d = data.getInt(last - mo);
                    p = ((long) seed3 + c) * ((long) seed4 + d);
                    seed3 = (int) p;
                    seed4 = (int) (p >>> 32);
                }
            } else {
                a = 0;
                b = 0;
                if (len != 0) {
                    a = data.get(i);
                    if (len != 1) {
                        a ^= data.get(i + 1) << 8;
                        if (len != 2) {
                            a ^= data.get(i + 2) << 16;
                        }
                    }
                }

            }
        } else {
            val01 ^= seed1;
            val10 ^= seed2;

            do {
                final int s1 = seed1;
                final int s4 = seed4;

                p = ((long) seed1 + data.getInt(i)) * ((long) seed2 + data.getInt(i + 4));
                seed1 = (int) p;
                seed2 = (int) (p >>> 32);
                p = ((long) seed3 + data.getInt(i + 8)) * ((long) seed4 + data.getInt(i + 12));
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);

                len -= 16;
                i += 16;

                seed1 += val01;
                seed2 += s4;
                seed3 += s1;
                seed4 += val10;
            } while (len > 16);

            a = data.getInt(i + len - 8);
            b = data.getInt(i + len - 4);

            if (len > 8) {
                c = data.getInt(i + len - 16);
                d = data.getInt(i + len - 12);

                p = ((long) seed3 + c) * ((long) seed4 + d);
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);
            }
        }
        seed1 ^= seed3;
        seed2 ^= seed4;

        p = ((long) seed1 + a) * ((long) seed2 + b);
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);

        p = ((long) seed1 ^ val01) * ((long) seed2);
        a = (int) p;
        b = (int) (p >>> 32);

        return a ^ b;
    }

    /**
     * Meant to handle hashing larger 2D arrays (or higher dimensions), this lets you pass a {@link Hasher.HashFunction} as
     * the first parameter, and then this uses that function to get a hash for each T item in data. T is usually an
     * array type, and function is usually a method reference to a {@link #hashA5} method here.
     *
     * @param seed any long seed
     * @param function typically a method reference to a {@link #hashA5} method here
     * @param data     input array
     * @param <T>      typically an array type, often of primitive items; may be more than one-dimensional
     * @return the 32-bit hash of data
     */
    public static <T> int hashA5(final long seed, final Hasher.HashFunction<T> function, final T[] data) {
        if (data == null) return 0;
        return hashA5(seed, function, data, 0, data.length);
    }

    /**
     * Meant to handle hashing larger 2D arrays (or higher dimensions), this lets you pass a {@link Hasher.HashFunction} as
     * the first parameter, and then this uses that function to get a hash for each T item in data. T is usually an
     * array type, and function is usually a method reference to a {@link #hashA5} method here.
     *
     * @param seed any long seed
     * @param function typically a method reference to a {@link #hashA5} method here
     * @param data     input array
     * @param start    starting index in data
     * @param length   how many items to use from data
     * @param <T>      typically an array type, often of primitive items; may be more than one-dimensional
     * @return the 32-bit hash of data
     */
    public static <T> int hashA5(final long seed, final Hasher.HashFunction<T> function, final T[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        int len = Math.min(length, data.length - start);
        int val01 = VAL01;
        int val10 = VAL10;

        int seed1 = 0x243F6A88 ^ len;
        int seed2 = 0x85A308D3 ^ len;
        int seed3 = 0xFB0BD3EA;
        int seed4 = 0x0F58FD47;
        int a, b, c, d;
        long p;

        p = (seed2 ^ (seed & 0xFFFFFFFFL)) * (seed1 ^ (seed >>> 32));
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);
        p = (seed3 ^ (seed & 0xFFFFFFFFL)) * (seed4 ^ (seed >>> 32));
        seed3 = (int) p;
        seed4 = (int) (p >>> 32);

        int i = start;
        if (len <= 4) {
            if (len == 0) {
                a = 0;
                b = 0;
            } else {
                int last = i + len - 1;
                a = function.hash(data[i]);
                b = function.hash(data[last]);
                if (len > 2) {
                    int mo = len >>> 1;
                    c = function.hash(data[i + mo]);
                    d = function.hash(data[last - mo]);
                    p = ((long) seed3 + c) * ((long) seed4 + d);
                    seed3 = (int) p;
                    seed4 = (int) (p >>> 32);
                }
            }
        } else {
            val01 ^= seed1;
            val10 ^= seed2;

            do {
                final int s1 = seed1;
                final int s4 = seed4;

                p = ((long) seed1 + function.hash(data[i])) * ((long) seed2 + function.hash(data[i + 1]));
                seed1 = (int) p;
                seed2 = (int) (p >>> 32);
                p = ((long) seed3 + function.hash(data[i + 2])) * ((long) seed4 + function.hash(data[i + 3]));
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);

                len -= 4;
                i += 4;

                seed1 += val01;
                seed2 += s4;
                seed3 += s1;
                seed4 += val10;
            } while (len > 4);

            a = function.hash(data[i + len - 2]);
            b = function.hash(data[i + len - 1]);

            if (len > 2) {
                c = function.hash(data[i + len - 4]);
                d = function.hash(data[i + len - 3]);

                p = ((long) seed3 + c) * ((long) seed4 + d);
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);
            }
        }
        seed1 ^= seed3;
        seed2 ^= seed4;

        p = ((long) seed1 + a) * ((long) seed2 + b);
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);

        p = ((long) seed1 ^ val01) * ((long) seed2);
        a = (int) p;
        b = (int) (p >>> 32);

        return a ^ b;
    }

//</editor-fold>
//<editor-fold defaultstate="collapsed" desc="Boilerplate">
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HasherA5 hasher = (HasherA5) o;

        return seed == hasher.seed;
    }

    /**
     * Produces a String that holds the entire seed of this HasherA5. A HasherA5 is immutable, so to load the serialized
     * state you must create a new HasherA5 with {@link #deserializeFromString(CharSequence)}.
     *
     * @return a String holding the seed of this HasherA5, to be loaded by {@link #deserializeFromString(CharSequence)}
     */
    public String serializeToString() {
        return appendSerialized(new StringBuilder(11)).toString();
    }

    /**
     * Appends the textual form of this HasherA5 to the given StringBuilder, StringBuffer, CharBuffer, or similar.
     * You can recover this state from such a textual form by calling {@link #deserializeFromString(CharSequence)} to
     * create a new HasherA5.
     *
     * @param sb  an Appendable CharSequence that will be modified
     * @param <T> any type that is both a CharSequence and an Appendable, such as StringBuilder, StringBuffer, or CharBuffer
     * @return {@code sb}, for chaining
     */
    public <T extends CharSequence & Appendable> T appendSerialized(T sb) {
        Base.SIMPLE64.appendUnsigned(sb, seed);
        return sb;
    }

    /**
     * Given a String or other CharSequence produced by {@link #serializeToString()}, this creates a new HasherA5 with the
     * seed stored in the start of that CharSequence.
     *
     * @param data a String or other CharSequence produced by {@link #serializeToString()}
     * @return a new HasherA5 with a seed loaded from the given String or other CharSequence
     */
    public static HasherA5 deserializeFromString(CharSequence data) {
        return deserializeFromString(data, 0);
    }

    /**
     * Given a String or other CharSequence produced by {@link #serializeToString()} or
     * {@link #appendSerialized(CharSequence)} and an offset to indicate where to
     * read 11 chars from that CharSequence, this creates a new HasherA5 with the seed stored in that CharSequence.
     *
     * @param data   a String or other CharSequence produced by {@link #serializeToString()}
     * @param offset where to start reading the 11 chars of a serialized state from data
     * @return a new HasherA5 with a seed loaded from the given String or other CharSequence
     */
    public static HasherA5 deserializeFromString(CharSequence data, int offset) {
        if (data == null || offset < 0 || data.length() - offset < 11) return HasherA5.hydrogen;
        return new HasherA5(Base.SIMPLE64.readLong(data, offset, offset + 11));
    }

    /**
     * This shouldn't ever be necessary, because a HasherA5 is entirely immutable, but if for some reason you need a
     * duplicate of an existing HasherA5, this exists. Normally you can just reference an existing HasherA5, though!
     *
     * @return a new HasherA5 with the same seed as this one
     */
    public HasherA5 copy() {
        return new HasherA5(seed);
    }

    @Override
    public int hashCode() {
        return (int) (seed ^ (seed >>> 32));
    }

    @Override
    public String toString() {
        return "HasherA5{" +
                "seed=" + seed +
                '}';
    }
//</editor-fold>
}
