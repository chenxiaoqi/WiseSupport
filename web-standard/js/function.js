(function () {
    function* generator(i) {
        while (true) {
            yield i++
        }
    }

    let gen = generator(10);
    console.log(gen.next().value);
    console.log(gen.next().value);
    console.log(gen.next().value);

    function returnAfter2Seconds() {
        return new Promise(function (resolve, reject) {
            setTimeout(function () {
                resolve('resolved')
            }, 2000)
        })
    }

    async function asyncCall() {
        console.log('calling');
        const result = await returnAfter2Seconds();
        console.log(result)
    }

    asyncCall();
    console.log('do something concurrently!')

    let [a, b] = [1, 2];
    console.log(a, b)

})();