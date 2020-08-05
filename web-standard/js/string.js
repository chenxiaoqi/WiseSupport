(function () {
    let o1 = {a: 1, b: 1, c: 1};
    let o2 = {a: 2, d: 1, ...o1};
    console.log(o2);

    let a1 = [1, 2, 3, 4];
    let a2 = [1, 2, 3, ...a1];
    console.log(a2);


    let a = 1, b = 2;
    let add = function (a, b) {
        return a + b;
    };
    let msg = `${a} + ${b} = ${a + b} or ${add(a, b)}`;
    console.log(msg);

    function isLarge() {
        return false;
    }

    let classes = `header ${isLarge() ? '' : `icon-${isLarge() ? 'true' : 'false'}`}`;
    console.log(classes);

    let person = 'Mike';
    let age = 19;

    function myTag(strings, personExpr, ageExpr) {
        return strings[0] + personExpr + strings[1] + ageExpr;
    }

    console.log(myTag`that ${person}'s age is ${age}`);
})();
