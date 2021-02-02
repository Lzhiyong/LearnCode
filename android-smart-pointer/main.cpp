#include <iostream>
#include <string>
#include "utils/RefBase.h"

using namespace std;
using namespace android;

// Android智能指针的使用
class Person : public RefBase {

  public:
    Person(string name = "") {
        // 构造函数
        this->name = name;
        cout << "Pserson(" << name << ") 构造函数调用" << endl;
    }

    virtual ~Person() {
        /*
         * 析构函数，这里的析构函数可以定义成虚析构函数
         * 也可以定义成普通的析构函数
         * 如果Person是作为基类的话就一定要定义成虚析构函数
         */
        cout << this->name << "~Person() 析构函数调用" << endl;
        
    }

    void setA(sp<Person> &A) {
        this->A = A;
        weak = A->getWeakRefs();
        cout << "A的弱引用计数：" << weak->getWeakCount() << endl;
    }

    void setB(sp<Person> &B) {
        this->B = B;
        weak = B->getWeakRefs();
        cout << "B的弱引用计数：" << weak->getWeakCount() << endl;
    }

    string& getName(void) { return name; }

    void printInfo(void) {
        // 把A B强制升级为强引用
        // 因为弱引用只记录了对象的引用计数，不能访问类的成员属性和方法
        sp<Person> a = A.promote();
        sp<Person> b = B.promote();

        // 如果对象被销毁了，就不能升级为强引用 promote函数就会返回NULL
        if (a != NULL) {
            cout << "A:  " << a->getName() << endl;
            cout << "A的强引用计数：" << a->getStrongCount() << endl;
        }

        if (b != NULL) {
            cout << "B:  " << b->getName() << endl;
            cout << "B的强引用计数：" << b->getStrongCount() << endl;
        }
    }

    private:
      string name;
      //定义A B两个人
      wp<Person> A;
      wp<Person> B;
      weakref_type *weak;
};

void test_func() {
    // 注意这里不要写成  sp<Person> *A = new Person()
    // 也不要写成 sp<Person> A = new sp<Person>();

    // 可以写成sp<Person> A(new Person())这种写法是对的
    // A本身是一个对象，只是重载了->操作符，这个重载函数返回一个Person的指针对象
    // 所以才可以以指针方式调用Person的成员函数，A->printInfo
    sp<Person> A = new Person("张三");
    sp<Person> B = new Person("李四");
    cout << "#################################" << endl;
    
    // 这里设置A B两个对象相互引用
    // 如果在这里使用轻量级LightRefBase智能指针，test_func函数在调用返回后，A B两个对象都不会被自动删除
    // 对于对象之间的相互引用，只能使用强弱指针的方式
    A->setB(B);
    B->setA(A);

    A->printInfo();
    B->printInfo();

    cout << "#################################" << endl;
}

int main(int argc, char **argv) {
    // test_func方法调用完之后，A B对象会自动delete
    test_func();
    return 0;
}
