function sayHello() {
    console.log('hello');
}

function sayWorld() {
    console.log('world')
}

function greeting() {
    console.log("welcome!");
}
// export default greeting;

export default function () {
    console.log("welcome!");
}

export {sayHello, sayWorld};


export function draw() {
    console.log('draw');
    return {
        a: 1
    }
}
