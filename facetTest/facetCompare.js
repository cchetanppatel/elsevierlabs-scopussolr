var fs = require('fs');
 
var walkPath = './';
 
var walk = function (dir, done) {
    fs.readdir(dir, function (error, list) {
        if (error) {
            return done(error);
        }
 
        var i = 0;
 
        (function next () {
            var file = list[i++];
 
            if (!file) {
                return done(null);
            }
            
            file20Name = dir + '/' + file;
            file200Name = file20Name.replace('facet20','facet200');
            console.log(file20Name);
            console.log(file200Name);
            
            // Open the 2 files
            var file20 = fs.readFileSync(file20Name, "utf8");
            var file200 = fs.readFileSync(file200Name, "utf8");
            
            // Create JSON objects
            var file20Json = JSON.parse(file20);
            var file200Json = JSON.parse(file200);
            
            // Compare Query 
            if (file20Json.responseHeader.params.q !== file200Json.responseHeader.params.q) {
                console.log('query is not the same');
                console.log(file20Json.responseHeader.params.q);
                console.log(file200Json.responseHeader.params.q);
            }
            
            // Compare Num Found
            if (file20Json.response.numFound !== file200Json.response.numFound) {
                console.log('numFound is not the same');
                console.log(file20Json.response.numFound);
                console.log(file200Json.response.numFound);
            }

            
            // Compare pubyr facet
            var pubyrArray = file20Json.facet_counts.facet_fields['pubyr-f'];
            var pubyrLength = pubyrArray.length;
            for (var f = 0; f < pubyrLength; f++) {
                if (pubyrArray[f] !== file200Json.facet_counts.facet_fields['pubyr-f'][f]) {
                    console.log('pubyr [' + f + '] is not the same');
                    console.log(pubyrArray[f] + ' vs ' + file200Json.facet_counts.facet_fields['pubyr-f'][f]);
                    file200Json.facet_counts.facet_fields['pubyr-f'][f]
                }   
            }

            // Compare prefnameauid facet
            var prefnameauidArray = file20Json.facet_counts.facet_fields['prefnameauid-f'];
            var prefnameauidLength = prefnameauidArray.length;
            for (var f = 0; f < prefnameauidLength; f++) {
                if (prefnameauidArray[f] !== file200Json.facet_counts.facet_fields['prefnameauid-f'][f]) {
                    console.log('prefnameauid [' + f + '] is not the same');
                    console.log(prefnameauidArray[f] + ' vs ' + file200Json.facet_counts.facet_fields['prefnameauid-f'][f]);
                    file200Json.facet_counts.facet_fields['prefnameauid-f'][f]
                }   
            }

            // Compare subjabbr facet
            var subjabbrArray = file20Json.facet_counts.facet_fields['subjabbr-f'];
            var subjabbrLength = subjabbrArray.length;
            for (var f = 0; f < subjabbrLength; f++) {
                if (subjabbrArray[f] !== file200Json.facet_counts.facet_fields['subjabbr-f'][f]) {
                    console.log('subjabbr [' + f + '] is not the same');
                    console.log(subjabbrArray[f] + ' vs ' + file200Json.facet_counts.facet_fields['subjabbr-f'][f]);
                    file200Json.facet_counts.facet_fields['subjabbr-f'][f]
                }   
            }

            // Compare subtype facet
            var subtypeArray = file20Json.facet_counts.facet_fields['subtype-f'];
            var subtypeLength = subtypeArray.length;
            for (var f = 0; f < subtypeLength; f++) {
                if (subtypeArray[f] !== file200Json.facet_counts.facet_fields['subtype-f'][f]) {
                    console.log('subtype [' + f + '] is not the same');
                    console.log(subtypeArray[f] + ' vs ' + file200Json.facet_counts.facet_fields['subtype-f'][f]);
                    file200Json.facet_counts.facet_fields['subtype-f'][f]
                }   
            }

            // Compare exactsrctitle facet
            var exactsrctitleArray = file20Json.facet_counts.facet_fields['exactsrctitle-f'];
            var exactsrctitleLength = exactsrctitleArray.length;
            for (var f = 0; f < exactsrctitleLength; f++) {
                if (exactsrctitleArray[f] !== file200Json.facet_counts.facet_fields['exactsrctitle-f'][f]) {
                    console.log('exactsrctitle [' + f + '] is not the same');
                    console.log(exactsrctitleArray[f] + ' vs ' + file200Json.facet_counts.facet_fields['exactsrctitle-f'][f]);
                    file200Json.facet_counts.facet_fields['exactsrctitle-f'][f]
                }   
            }

            // Compare exactkeyword facet
            var exactkeywordArray = file20Json.facet_counts.facet_fields['exactkeyword-f'];
            var exactkeywordLength = exactkeywordArray.length;
            for (var f = 0; f < exactkeywordLength; f++) {
                if (exactkeywordArray[f] !== file200Json.facet_counts.facet_fields['exactkeyword-f'][f]) {
                    console.log('exactkeyword [' + f + '] is not the same');
                    console.log(exactkeywordArray[f] + ' vs ' + file200Json.facet_counts.facet_fields['exactkeyword-f'][f]);
                    file200Json.facet_counts.facet_fields['exactkeyword-f'][f]
                }   
            }

            // Compare afid facet
            var afidArray = file20Json.facet_counts.facet_fields['afid-f'];
            var afidLength = afidArray.length;
            for (var f = 0; f < afidLength; f++) {
                if (afidArray[f] !== file200Json.facet_counts.facet_fields['afid-f'][f]) {
                    console.log('afid [' + f + '] is not the same');
                    console.log(afidArray[f] + ' vs ' + file200Json.facet_counts.facet_fields['afid-f'][f]);
                    file200Json.facet_counts.facet_fields['afid-f'][f]
                }   
            }

            // Compare affilctry facet
            var affilctryArray = file20Json.facet_counts.facet_fields['affilctry-f'];
            var affilctryLength = affilctryArray.length;
            for (var f = 0; f < affilctryLength; f++) {
                if (affilctryArray[f] !== file200Json.facet_counts.facet_fields['affilctry-f'][f]) {
                    console.log('affilctry [' + f + '] is not the same');
                    console.log(affilctryArray[f] + ' vs ' + file200Json.facet_counts.facet_fields['affilctry-f'][f]);
                    file200Json.facet_counts.facet_fields['affilctry-f'][f]
                }   
            }

            // Compare srctype facet
            var srctypeArray = file20Json.facet_counts.facet_fields['srctype-f'];
            var srctypeLength = srctypeArray.length;
            for (var f = 0; f < srctypeLength; f++) {
                if (srctypeArray[f] !== file200Json.facet_counts.facet_fields['srctype-f'][f]) {
                    console.log('srctype [' + f + '] is not the same');
                    console.log(srctypeArray[f] + ' vs ' + file200Json.facet_counts.facet_fields['srctype-f'][f]);
                    file200Json.facet_counts.facet_fields['srctype-f'][f]
                }   
            }

            // Compare lang facet
            var langArray = file20Json.facet_counts.facet_fields['lang-f'];
            var langLength = langArray.length;
            for (var f = 0; f < langLength; f++) {
                if (langArray[f] !== file200Json.facet_counts.facet_fields['lang-f'][f]) {
                    console.log('lang [' + f + '] is not the same');
                    console.log(langArray[f] + ' vs ' + file200Json.facet_counts.facet_fields['lang-f'][f]);
                    file200Json.facet_counts.facet_fields['lang-f'][f]
                }   
            }

            next();
            
        })();
    });
};
 
 
console.log('-------------------------------------------------------------');
console.log('processing...');
console.log('-------------------------------------------------------------');
 
walk('/Users/mcbeathd/Desktop/facetTest/facet20', function(error) {
    if (error) {
        throw error;
    } else {
        console.log('-------------------------------------------------------------');
        console.log('finished.');
        console.log('-------------------------------------------------------------');
    }
});
