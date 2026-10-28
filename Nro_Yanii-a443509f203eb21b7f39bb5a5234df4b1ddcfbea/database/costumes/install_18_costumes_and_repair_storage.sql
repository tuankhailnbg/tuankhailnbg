-- NRO Yanii | Batch 01 | 18 costumes + Santa storage shop repair
-- Target: MariaDB database ngocrong. Execute only after backing up ngocrong.
-- Requires import source to match item_template 0..2153 and part 0..2041.
-- Download and verify all 4x costume sprite zooms before running.
SET NAMES utf8mb4;
DELIMITER $$
DROP PROCEDURE IF EXISTS nro_install_costumes_20261028$$
CREATE PROCEDURE nro_install_costumes_20261028()
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    IF (SELECT COUNT(*) FROM item_template) <> 2154
       OR (SELECT MAX(id) FROM item_template) <> 2153 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT='ITEM_TEMPLATE differs from base dump; stop to avoid ID collision';
    END IF;
    IF (SELECT COUNT(*) FROM part) <> 2042
       OR (SELECT MAX(id) FROM part) <> 2041 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT='PART differs from base dump; stop to avoid broken part indexes';
    END IF;
    IF EXISTS (SELECT 1 FROM cai_trang WHERE id_temp BETWEEN 2154 AND 2171)
       OR EXISTS (SELECT 1 FROM head_avatar WHERE head_id BETWEEN 2042 AND 2095) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT='Costume mapping/head avatar ID collision; import cancelled';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM shop WHERE id=21 AND npc_id=39 AND shop_order=2)
       OR NOT EXISTS (SELECT 1 FROM tab_shop WHERE id=37 AND shop_id=21) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT='Santa storage shop 21/tab 37 missing from database';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM item_template WHERE id=517 AND `TYPE`=27)
       OR NOT EXISTS (SELECT 1 FROM item_template WHERE id=518 AND `TYPE`=27) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT='Storage expansion templates 517/518 invalid';
    END IF;
    START TRANSACTION;
        -- BATCH01A
        INSERT INTO part (id,TYPE,DATA) VALUES (2042,0,'[[32245,1,-15],[32246,-1,-14],[2955,0,0]]');
        INSERT INTO part (id,TYPE,DATA) VALUES (2043,1,'[[32247,-5,-10],[32248,-9,-21],[32249,-4,-12],[32250,-8,-15],[32251,-9,-14],[32252,-5,-12],[32253,-5,-15],[32254,-8,-11],[32255,-2,-13],[32256,-2,-13],[32257,-2,-12],[32258,-7,-13],[32259,-4,-12],[32260,-5,-10],[32261,-1,-13],[32262,-7,-11],[2955,0,0]]');
        INSERT INTO part (id,TYPE,DATA) VALUES (2044,2,'[[32263,4,-3],[32264,-3,-5],[32265,2,0],[32266,1,-2],[32267,-1,-1],[32268,-1,1],[32269,2,-1],[32270,-1,-1],[32271,-1,-3],[32272,-2,-3],[32273,-4,-2],[32274,-3,1],[32275,-3,-3],[2955,0,0]]');
        INSERT INTO head_avatar (head_id,avatar_id) VALUES (2042,32276);
        INSERT INTO item_template (id,TYPE,gender,NAME,description,icon_id,part,is_up_to_up,power_require) VALUES (2154,5,3,'Cải trang Captain America','Cải trang Captain America',32245,-1,0,0);
        INSERT INTO cai_trang (id_temp,head,body,leg,bag) VALUES (2154,2042,2043,2044,-1);
        INSERT INTO part (id,TYPE,DATA) VALUES (2045,0,'[[32277,-2,-14],[32278,1,-11],[2955,0,0]]');
        INSERT INTO part (id,TYPE,DATA) VALUES (2046,1,'[[32279,-6,-14],[32280,-12,-17],[32281,-6,-11],[32282,-4,-12],[32283,-7,-10],[32284,-1,-12],[32285,-4,-12],[32286,-12,-11],[32287,-7,-12],[32288,-5,-13],[32289,-1,-10],[32290,-7,-13],[32291,-3,-8],[32292,-3,-7],[32293,-4,-8],[32294,-6,-8],[2955,0,0]]');
        INSERT INTO part (id,TYPE,DATA) VALUES (2047,2,'[[32295,-3,-6],[32296,-7,-4],[32297,1,-1],[32298,1,-1],[32299,0,0],[32300,-2,0],[32301,2,-1],[32302,-4,-3],[32303,-6,-3],[32304,-1,-3],[32305,-5,-4],[32306,-5,-1],[32307,-6,-3],[2955,0,0]]');
        INSERT INTO head_avatar (head_id,avatar_id) VALUES (2045,32308);
        INSERT INTO item_template (id,TYPE,gender,NAME,description,icon_id,part,is_up_to_up,power_require) VALUES (2155,5,3,'Cải trang Spider Man','Cải trang Spider Man',32277,-1,0,0);
        INSERT INTO cai_trang (id_temp,head,body,leg,bag) VALUES (2155,2045,2046,2047,-1);
        INSERT INTO part (id,TYPE,DATA) VALUES (2048,0,'[[32309,-2,-12],[32310,-1,-14],[2955,0,0]]');
        INSERT INTO part (id,TYPE,DATA) VALUES (2049,1,'[[32311,-10,-7],[32312,-12,-16],[32313,-12,-16],[32314,-5,-12],[32315,-8,-16],[32316,-4,-14],[32317,-6,-13],[32318,-9,-10],[32319,-7,-12],[32320,-6,-12],[32321,-8,-12],[32322,-9,-14],[32323,-2,-15],[32324,-2,-12],[32325,-6,-9],[32326,-10,-11],[2955,0,0]]');
        INSERT INTO part (id,TYPE,DATA) VALUES (2050,2,'[[32327,-1,-4],[32328,-6,-4],[32329,0,-3],[32330,1,-1],[32331,-1,-4],[32332,-1,-2],[32333,1,-1],[32334,-3,0],[32335,-4,-1],[32336,-2,-4],[32337,-5,-2],[32338,-5,0],[32339,-6,-3],[2955,0,0]]');
        INSERT INTO head_avatar (head_id,avatar_id) VALUES (2048,32340);
        INSERT INTO item_template (id,TYPE,gender,NAME,description,icon_id,part,is_up_to_up,power_require) VALUES (2156,5,3,'Cải trang Hulk','Cải trang Hulk',32309,-1,0,0);
        INSERT INTO cai_trang (id_temp,head,body,leg,bag) VALUES (2156,2048,2049,2050,-1);
        INSERT INTO part (id,TYPE,DATA) VALUES (2051,0,'[[32341,-4,-9],[32342,-2,-7],[2955,0,0]]');
        INSERT INTO part (id,TYPE,DATA) VALUES (2052,1,'[[32344,-3,-6],[32343,2,-1],[32345,-1,-4],[32346,6,-4],[32347,4,-4],[32348,6,-3],[32349,5,-4],[32350,-3,-10],[32351,-9,-5],[32352,-3,-6],[32353,3,-3],[32354,0,-2],[32355,1,-3],[32356,0,-4],[32357,1,-3],[32358,2,-4],[2955,0,0]]');
        INSERT INTO part (id,TYPE,DATA) VALUES (2053,2,'[[32360,-2,-3],[32359,-1,-1],[32361,-1,-2],[32362,7,0],[32363,-3,-2],[32364,3,0],[32365,1,-1],[32366,0,1],[32367,-10,-1],[32368,-2,-3],[32369,-4,1],[32370,-7,0],[32371,-4,2],[32372,-4,2]]');
        INSERT INTO head_avatar (head_id,avatar_id) VALUES (2051,32373);
        INSERT INTO item_template (id,TYPE,gender,NAME,description,icon_id,part,is_up_to_up,power_require) VALUES (2157,5,3,'Cải trang Iron Man','Cải trang Iron Man',32341,-1,0,0);
        INSERT INTO cai_trang (id_temp,head,body,leg,bag) VALUES (2157,2051,2052,2053,-1);
        INSERT INTO part (id,TYPE,DATA) VALUES (2054,0,'[[32212,-4,-9],[32213,-2,-7],[2955,0,0]]');
        INSERT INTO part (id,TYPE,DATA) VALUES (2055,1,'[[32215,-3,-6],[32214,2,-1],[32216,-1,-4],[32217,6,-4],[32218,4,-4],[32219,6,-3],[32220,5,-4],[32221,-3,-10],[32222,-9,-5],[32223,-3,-6],[32224,3,-3],[32225,0,-2],[32226,1,-3],[32227,0,-4],[32228,1,-3],[32229,2,-4],[2955,0,0]]');
        INSERT INTO part (id,TYPE,DATA) VALUES (2056,2,'[[32231,-2,-3],[32230,-1,-1],[32232,-1,-2],[32233,7,0],[32234,-3,-2],[32235,3,0],[32236,1,-1],[32237,0,1],[32238,-10,-1],[32239,-2,-3],[32240,-4,1],[32241,-7,0],[32242,-4,2],[32243,-4,2]]');
        INSERT INTO head_avatar (head_id,avatar_id) VALUES (2054,32244);
        INSERT INTO item_template (id,TYPE,gender,NAME,description,icon_id,part,is_up_to_up,power_require) VALUES (2158,5,3,'Cải trang Black Parten','Cải trang Black Parten',32212,-1,0,0);
        INSERT INTO cai_trang (id_temp,head,body,leg,bag) VALUES (2158,2054,2055,2056,-1);
        INSERT INTO part (id,TYPE,DATA) VALUES (2057,0,'[[32374,4,-5],[32375,5,-3],[2955,0,0]]');
        INSERT INTO part (id,TYPE,DATA) VALUES (2058,1,'[[32376,4,1],[32377,0,-4],[32378,6,-2],[32379,4,-1],[32380,7,0],[32381,10,1],[32382,8,0],[32383,1,-2],[32384,1,-1],[32385,0,-2],[32386,6,0],[32387,3,0],[32388,3,1],[32389,5,0],[32390,3,-2],[32391,6,-2],[2955,0,0]]');
        INSERT INTO part (id,TYPE,DATA) VALUES (2059,2,'[[32392,4,-4],[32393,1,-2],[32394,8,1],[32395,5,1],[32396,4,2],[32397,6,2],[32398,7,1],[32399,3,0],[32400,1,1],[32401,4,-1],[32402,-1,3],[32403,-2,4],[32404,-3,2],[2955,0,0]]');
        INSERT INTO head_avatar (head_id,avatar_id) VALUES (2057,32405);
        INSERT INTO item_template (id,TYPE,gender,NAME,description,icon_id,part,is_up_to_up,power_require) VALUES (2159,5,3,'Cải trang Gojo','Cải trang Gojo',32374,-1,0,0);
        INSERT INTO cai_trang (id_temp,head,body,leg,bag) VALUES (2159,2057,2058,2059,-1);

        -- BATCH01B
        INSERT INTO part (id,TYPE,DATA) VALUES (2060,0,'[[32453,0,0],[32453,0,0],[32453,0,0]]');
        INSERT INTO part (id,TYPE,DATA) VALUES (2061,1,'[[32446,-12,-45],[32498,-10,-45],[32499,-16,-39],[32447,-11,-44],[32448,-12,-44],[32449,-14,-44],[32450,-10,-46],[32500,-16,-39],[32488,-15,-40],[32489,-21,-46],[32490,-14,-40],[32491,-11,-43],[32492,-20,-42],[32501,-16,-40],[32493,-18,-47],[32451,-12,-45],[32452,-16,-40]]');
        INSERT INTO part (id,TYPE,DATA) VALUES (2062,2,'[[32453,0,0],[32453,0,0],[32453,0,0],[32453,0,0],[32453,0,0],[32453,0,0],[32453,0,0],[32453,0,0],[32453,0,0],[32453,0,0],[32453,0,0],[32453,0,0],[32453,0,0],[32453,0,0]]');
        INSERT INTO head_avatar (head_id,avatar_id) VALUES (2060,32455);
        INSERT INTO item_template (id,TYPE,gender,NAME,description,icon_id,part,is_up_to_up,power_require) VALUES (2160,5,3,'Cải trang Akaza','Cải trang Akaza',32454,-1,0,0);
        INSERT INTO cai_trang (id_temp,head,body,leg,bag) VALUES (2160,2060,2061,2062,-1);
        INSERT INTO part (id,TYPE,DATA) VALUES (2063,0,'[[32470,0,0],[32470,0,0],[32470,0,0]]');
        INSERT INTO part (id,TYPE,DATA) VALUES (2064,1,'[[32456,-20,-42],[32502,-14,-45],[32503,-19,-42],[32457,-14,-24],[32458,-16,-39],[32459,-16,-39],[32460,-14,-46],[32504,-19,-42],[32461,-8,-34],[32462,-12,-32],[32463,-6,-38],[32464,-10,-41],[32465,-6,-38],[32466,-18,-34],[32467,-12,-32],[32468,-15,-41],[32469,-14,-46]]');
        INSERT INTO part (id,TYPE,DATA) VALUES (2065,2,'[[32470,0,0],[32470,0,0],[32470,0,0],[32470,0,0],[32470,0,0],[32470,0,0],[32470,0,0],[32470,0,0],[32470,0,0],[32470,0,0],[32470,0,0],[32470,0,0],[32470,0,0],[32470,0,0]]');
        INSERT INTO head_avatar (head_id,avatar_id) VALUES (2063,32472);
        INSERT INTO item_template (id,TYPE,gender,NAME,description,icon_id,part,is_up_to_up,power_require) VALUES (2161,5,3,'Cải trang Douma','Cải trang Douma',32471,-1,0,0);
        INSERT INTO cai_trang (id_temp,head,body,leg,bag) VALUES (2161,2063,2064,2065,-1);
        INSERT INTO part (id,TYPE,DATA) VALUES (2066,0,'[[32485,0,0],[32485,0,0],[32485,0,0]]');
        INSERT INTO part (id,TYPE,DATA) VALUES (2067,1,'[[32473,-13,-42],[32474,-16,-37],[32475,-6,-27],[32476,-8,-29],[32477,-8,-27],[32478,-8,-41],[32479,-8,-40],[32480,-6,-27],[32494,-11,-22],[32495,-13,-38],[32481,-3,-25],[32482,-11,-33],[32496,-11,-37],[32505,-14,-40],[32497,-14,-40],[32483,-14,-38],[32484,-8,-40]]');
        INSERT INTO part (id,TYPE,DATA) VALUES (2068,2,'[[32485,0,0],[32485,0,0],[32485,0,0],[32485,0,0],[32485,0,0],[32485,0,0],[32485,0,0],[32485,0,0],[32485,0,0],[32485,0,0],[32485,0,0],[32485,0,0],[32485,0,0],[32485,0,0]]');
        INSERT INTO head_avatar (head_id,avatar_id) VALUES (2066,32487);
        INSERT INTO item_template (id,TYPE,gender,NAME,description,icon_id,part,is_up_to_up,power_require) VALUES (2162,5,3,'Cải trang Kokushibo','Cải trang Kokushibo',32486,-1,0,0);
        INSERT INTO cai_trang (id_temp,head,body,leg,bag) VALUES (2162,2066,2067,2068,-1);
        INSERT INTO part (id,TYPE,DATA) VALUES (2069,0,'[[32518,0,0],[32518,0,0],[32518,0,0]]');
        INSERT INTO part (id,TYPE,DATA) VALUES (2070,1,'[[32506,-23,-46],[32521,-19,-44],[32522,-18,-45],[32507,-18,-29],[32508,-19,-43],[32523,-14,-41],[32509,-19,-47],[32524,-18,-45],[32510,-19,-43],[32511,-13,-43],[32512,-14,-42],[32513,-13,-39],[32514,-13,-43],[32515,-12,-43],[32516,-18,-46],[32517,-18,-43],[32525,-19,-43]]');
        INSERT INTO part (id,TYPE,DATA) VALUES (2071,2,'[[32518,0,0],[32518,0,0],[32518,0,0],[32518,0,0],[32518,0,0],[32518,0,0],[32518,0,0],[32518,0,0],[32518,0,0],[32518,0,0],[32518,0,0],[32518,0,0],[32518,0,0],[32518,0,0]]');
        INSERT INTO head_avatar (head_id,avatar_id) VALUES (2069,32520);
        INSERT INTO item_template (id,TYPE,gender,NAME,description,icon_id,part,is_up_to_up,power_require) VALUES (2163,5,3,'Cải trang Kibutsuji Muzan','Cải trang Kibutsuji Muzan',32519,-1,0,0);
        INSERT INTO cai_trang (id_temp,head,body,leg,bag) VALUES (2163,2069,2070,2071,-1);
        INSERT INTO part (id,TYPE,DATA) VALUES (2072,0,'[[32420,0,0],[32420,0,0],[32420,0,0]]');
        INSERT INTO part (id,TYPE,DATA) VALUES (2073,1,'[[32406,-24,-35],[32422,-9,-36],[32423,-15,-33],[32407,-14,-24],[32408,-17,-34],[32409,-18,-31],[32410,-16,-38],[32443,-15,-33],[32411,-12,-32],[32412,-16,-43],[32413,-12,-20],[32414,-9,-33],[32415,-10,-23],[32416,-15,-30],[32417,-15,-37],[32418,-21,-39],[32419,-17,-23]]');
        INSERT INTO part (id,TYPE,DATA) VALUES (2074,2,'[[32420,0,0],[32420,0,0],[32420,0,0],[32420,0,0],[32420,0,0],[32420,0,0],[32420,0,0],[32420,0,0],[32420,0,0],[32420,0,0],[32420,0,0],[32420,0,0],[32420,0,0],[32420,0,0]]');
        INSERT INTO head_avatar (head_id,avatar_id) VALUES (2072,32444);
        INSERT INTO item_template (id,TYPE,gender,NAME,description,icon_id,part,is_up_to_up,power_require) VALUES (2164,5,3,'Cải trang Rengoku','Cải trang Rengoku',32421,-1,0,0);
        INSERT INTO cai_trang (id_temp,head,body,leg,bag) VALUES (2164,2072,2073,2074,-1);
        INSERT INTO part (id,TYPE,DATA) VALUES (2075,0,'[[32432,0,0],[32432,0,0],[32432,0,0]]');
        INSERT INTO part (id,TYPE,DATA) VALUES (2076,1,'[[32424,-21,-33],[32434,-7,-38],[32435,-20,-43],[32436,-12,-29],[32437,-12,-29],[32438,-13,-41],[32439,-10,-40],[32442,-20,-43],[32425,-8,-30],[32426,-10,-30],[32427,-11,-41],[32428,-11,-42],[32429,-12,-42],[32440,-20,-43],[32430,-12,-41],[32431,-7,-42],[32441,-13,-41]]');
        INSERT INTO part (id,TYPE,DATA) VALUES (2077,2,'[[32432,0,0],[32432,0,0],[32432,0,0],[32432,0,0],[32432,0,0],[32432,0,0],[32432,0,0],[32432,0,0],[32432,0,0],[32432,0,0],[32432,0,0],[32432,0,0],[32432,0,0],[32432,0,0]]');
        INSERT INTO head_avatar (head_id,avatar_id) VALUES (2075,32445);
        INSERT INTO item_template (id,TYPE,gender,NAME,description,icon_id,part,is_up_to_up,power_require) VALUES (2165,5,3,'Cải trang Shinobu Kocho','Cải trang Shinobu Kocho',32433,-1,0,0);
        INSERT INTO cai_trang (id_temp,head,body,leg,bag) VALUES (2165,2075,2076,2077,-1);

        -- BATCH01C
        INSERT INTO part (id,TYPE,DATA) VALUES (2078,0,'[[32608,0,0],[32608,0,0],[32608,0,0]]');
        INSERT INTO part (id,TYPE,DATA) VALUES (2079,1,'[[32595,-17,-37],[32610,-7,-34],[32614,-13,-35],[32596,-10,-18],[32597,-12,-23],[32598,-13,-23],[32599,-12,-34],[32617,-13,-35],[32600,-10,-29],[32601,-11,-39],[32602,-11,-42],[32603,-11,-41],[32604,-10,-29],[32615,-13,-35],[32605,-9,-43],[32606,-12,-39],[32607,-12,-34]]');
        INSERT INTO part (id,TYPE,DATA) VALUES (2080,2,'[[32608,0,0],[32608,0,0],[32608,0,0],[32608,0,0],[32608,0,0],[32608,0,0],[32608,0,0],[32608,0,0],[32608,0,0],[32608,0,0],[32608,0,0],[32608,0,0],[32608,0,0],[32608,0,0]]');
        INSERT INTO head_avatar (head_id,avatar_id) VALUES (2078,32625);
        INSERT INTO item_template (id,TYPE,gender,NAME,description,icon_id,part,is_up_to_up,power_require) VALUES (2166,5,3,'Cải trang Mitsuri Kanroji','Cải trang Mitsuri Kanroji',32609,-1,0,0);
        INSERT INTO cai_trang (id_temp,head,body,leg,bag) VALUES (2166,2078,2079,2080,-1);
        INSERT INTO part (id,TYPE,DATA) VALUES (2081,0,'[[32575,0,0],[32575,0,0],[32575,0,0]]');
        INSERT INTO part (id,TYPE,DATA) VALUES (2082,1,'[[32562,-17,-28],[32611,-4,-38],[32612,-18,-34],[32563,-13,-22],[32564,-15,-28],[32565,-18,-30],[32566,-13,-26],[32620,-18,-34],[32567,-16,-40],[32568,-11,-33],[32569,-14,-30],[32570,-6,-34],[32571,-16,-40],[32613,-13,-40],[32572,-17,-34],[32573,-14,-31],[32574,-15,-28]]');
        INSERT INTO part (id,TYPE,DATA) VALUES (2083,2,'[[32575,0,0],[32575,0,0],[32575,0,0],[32575,0,0],[32575,0,0],[32575,0,0],[32575,0,0],[32575,0,0],[32575,0,0],[32575,0,0],[32575,0,0],[32575,0,0],[32575,0,0],[32575,0,0]]');
        INSERT INTO head_avatar (head_id,avatar_id) VALUES (2081,32623);
        INSERT INTO item_template (id,TYPE,gender,NAME,description,icon_id,part,is_up_to_up,power_require) VALUES (2167,5,3,'Cải trang Tengen Uzui','Cải trang Tengen Uzui',32576,-1,0,0);
        INSERT INTO cai_trang (id_temp,head,body,leg,bag) VALUES (2167,2081,2082,2083,-1);
        INSERT INTO part (id,TYPE,DATA) VALUES (2084,0,'[[32540,0,0],[32540,0,0],[32540,0,0]]');
        INSERT INTO part (id,TYPE,DATA) VALUES (2085,1,'[[32526,-22,-38],[32559,-4,-35],[32560,-15,-28],[32527,-13,-20],[32528,-17,-28],[32529,-16,-30],[32530,-12,-37],[32619,-16,-26],[32531,-11,-36],[32532,-13,-36],[32533,-11,-23],[32534,-12,-29],[32535,-7,-28],[32536,-16,-32],[32537,-18,-36],[32538,-16,-37],[32539,-16,-18]]');
        INSERT INTO part (id,TYPE,DATA) VALUES (2086,2,'[[32540,0,0],[32540,0,0],[32540,0,0],[32540,0,0],[32540,0,0],[32540,0,0],[32540,0,0],[32540,0,0],[32540,0,0],[32540,0,0],[32540,0,0],[32540,0,0],[32540,0,0],[32540,0,0]]');
        INSERT INTO head_avatar (head_id,avatar_id) VALUES (2084,32621);
        INSERT INTO item_template (id,TYPE,gender,NAME,description,icon_id,part,is_up_to_up,power_require) VALUES (2168,5,3,'Cải trang Tomioka Giyuu','Cải trang Tomioka Giyuu',32541,-1,0,0);
        INSERT INTO cai_trang (id_temp,head,body,leg,bag) VALUES (2168,2084,2085,2086,-1);
        INSERT INTO part (id,TYPE,DATA) VALUES (2087,0,'[[32635,0,0],[32635,0,0],[32635,0,0]]');
        INSERT INTO part (id,TYPE,DATA) VALUES (2088,1,'[[32626,-16,-46],[32638,-19,-44],[32644,-13,-36],[32639,-17,-24],[32640,-13,-36],[32641,-14,-41],[32642,-11,-44],[32645,-15,-46],[32627,-14,-44],[32628,-19,-43],[32629,-15,-29],[32630,-13,-45],[32631,-17,-44],[32632,-17,-47],[32633,-21,-46],[32634,-13,-30],[32643,-13,-37]]');
        INSERT INTO part (id,TYPE,DATA) VALUES (2089,2,'[[32635,0,0],[32635,0,0],[32635,0,0],[32635,0,0],[32635,0,0],[32635,0,0],[32635,0,0],[32635,0,0],[32635,0,0],[32635,0,0],[32635,0,0],[32635,0,0],[32635,0,0],[32635,0,0]]');
        INSERT INTO head_avatar (head_id,avatar_id) VALUES (2087,32637);
        INSERT INTO item_template (id,TYPE,gender,NAME,description,icon_id,part,is_up_to_up,power_require) VALUES (2169,5,3,'Cải trang Yoriichi','Cải trang Yoriichi',32636,-1,0,0);
        INSERT INTO cai_trang (id_temp,head,body,leg,bag) VALUES (2169,2087,2088,2089,-1);
        INSERT INTO part (id,TYPE,DATA) VALUES (2090,0,'[[32593,0,0],[32593,0,0],[32593,0,0]]');
        INSERT INTO part (id,TYPE,DATA) VALUES (2091,1,'[[32577,-17,-34],[32578,-9,-35],[32579,-14,-40],[32580,-11,-31],[32581,-11,-30],[32582,-11,-41],[32583,-12,-41],[32616,-14,-40],[32584,-12,-44],[32585,-11,-42],[32586,-8,-29],[32587,-11,-40],[32588,-12,-42],[32589,-9,-30],[32590,-12,-41],[32591,-5,-43],[32592,-10,-41]]');
        INSERT INTO part (id,TYPE,DATA) VALUES (2092,2,'[[32593,0,0],[32593,0,0],[32593,0,0],[32593,0,0],[32593,0,0],[32593,0,0],[32593,0,0],[32593,0,0],[32593,0,0],[32593,0,0],[32593,0,0],[32593,0,0],[32593,0,0],[32593,0,0]]');
        INSERT INTO head_avatar (head_id,avatar_id) VALUES (2090,32624);
        INSERT INTO item_template (id,TYPE,gender,NAME,description,icon_id,part,is_up_to_up,power_require) VALUES (2170,5,3,'Cải trang Gyomei Himejima','Cải trang Gyomei Himejima',32594,-1,0,0);
        INSERT INTO cai_trang (id_temp,head,body,leg,bag) VALUES (2170,2090,2091,2092,-1);
        INSERT INTO part (id,TYPE,DATA) VALUES (2093,0,'[[32557,0,0],[32557,0,0],[32557,0,0]]');
        INSERT INTO part (id,TYPE,DATA) VALUES (2094,1,'[[32542,-19,-36],[32543,-8,-37],[32561,-15,-32],[32544,-16,-34],[32545,-18,-32],[32546,-19,-37],[32547,-19,-36],[32618,-15,-32],[32548,-9,-30],[32549,-15,-40],[32550,-14,-28],[32551,-12,-38],[32552,-12,-45],[32553,-14,-26],[32554,-12,-44],[32555,-10,-41],[32556,-22,-38]]');
        INSERT INTO part (id,TYPE,DATA) VALUES (2095,2,'[[32557,0,0],[32557,0,0],[32557,0,0],[32557,0,0],[32557,0,0],[32557,0,0],[32557,0,0],[32557,0,0],[32557,0,0],[32557,0,0],[32557,0,0],[32557,0,0],[32557,0,0],[32557,0,0]]');
        INSERT INTO head_avatar (head_id,avatar_id) VALUES (2093,32622);
        INSERT INTO item_template (id,TYPE,gender,NAME,description,icon_id,part,is_up_to_up,power_require) VALUES (2171,5,3,'Cải trang Obanai Iguro','Cải trang Obanai Iguro',32558,-1,0,0);
        INSERT INTO cai_trang (id_temp,head,body,leg,bag) VALUES (2171,2093,2094,2095,-1);

    -- Keep Santa's expansion items available; Java recalculates progressive prices.
    UPDATE item_shop
    SET gold=0, gem=50, is_sell=1, item_exchange=-1, quantity_exchange=0
    WHERE tab_id=37 AND temp_id=517;
    IF NOT EXISTS (SELECT 1 FROM item_shop WHERE tab_id=37 AND temp_id=517) THEN
        INSERT INTO item_shop (tab_id,temp_id,gold,gem,is_new,is_sell,item_exchange,quantity_exchange)
        VALUES (37,517,0,50,0,1,-1,0);
    END IF;
    UPDATE item_shop
    SET gold=25000000, gem=0, is_sell=1, item_exchange=-1, quantity_exchange=0
    WHERE tab_id=37 AND temp_id=518;
    IF NOT EXISTS (SELECT 1 FROM item_shop WHERE tab_id=37 AND temp_id=518) THEN
        INSERT INTO item_shop (tab_id,temp_id,gold,gem,is_new,is_sell,item_exchange,quantity_exchange)
        VALUES (37,518,25000000,0,0,1,-1,0);
    END IF;
    COMMIT;
END$$
DELIMITER ;
CALL nro_install_costumes_20261028();
DROP PROCEDURE IF EXISTS nro_install_costumes_20261028;
-- Verify:
SELECT COUNT(*) AS costumes FROM item_template WHERE id BETWEEN 2154 AND 2171;
SELECT COUNT(*) AS outfit_mappings FROM cai_trang WHERE id_temp BETWEEN 2154 AND 2171;
SELECT COUNT(*) AS parts FROM part WHERE id BETWEEN 2042 AND 2095;
SELECT tab_id,temp_id,gold,gem,is_sell FROM item_shop WHERE tab_id=37 AND temp_id IN (517,518);
